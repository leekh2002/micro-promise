package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.GitHubBranchResponse;
import com.gyuhyuk.micro_promise.data.dto.GitHubCommitResponse;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubPushEvent;
import com.gyuhyuk.micro_promise.data.entity.BranchEntity;
import com.gyuhyuk.micro_promise.data.entity.CommitEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRepositoryEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.TaskBranchLinkEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskCommitLinkEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskEntity;
import com.gyuhyuk.micro_promise.repository.BranchRepository;
import com.gyuhyuk.micro_promise.repository.CommitRepository;
import com.gyuhyuk.micro_promise.repository.GitRepoRepository;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.TaskBranchLinkRepository;
import com.gyuhyuk.micro_promise.repository.TaskCommitLinkRepository;
import com.gyuhyuk.micro_promise.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitRepositorySyncService {

    private static final Logger log = LoggerFactory.getLogger(GitRepositorySyncService.class);
    private static final Pattern COMMIT_TASK_ID_PATTERN = Pattern.compile("#(\\d+)");
    private static final Pattern BRANCH_TASK_ID_PATTERN = Pattern.compile("(?:^|[/-])task-(\\d+)(?:$|[-/])");
    private static final int INITIAL_SYNC_COMMITS_PER_BRANCH = 100;

    private final GitHubClient gitHubClient;
    private final GitRepoRepository gitRepoRepository;
    private final BranchRepository branchRepository;
    private final CommitRepository commitRepository;
    private final TaskRepository taskRepository;
    // branch에 이미 연결된 task 목록을 조회해서 push된 commit을 자동 전파할 때 사용한다.
    private final TaskBranchLinkRepository taskBranchLinkRepository;
    // task-commit 링크 중복 여부를 확인하고 자동 링크를 저장할 때 사용한다.
    private final TaskCommitLinkRepository taskCommitLinkRepository;
    // 자동 생성되는 task-commit 링크의 linkedBy를 채우기 위해 프로젝트 owner를 조회한다.
    private final ProjectMemberRepository projectMemberRepository;

    public GitRepositorySyncService(GitHubClient gitHubClient,
                                    GitRepoRepository gitRepoRepository,
                                    BranchRepository branchRepository,
                                    CommitRepository commitRepository,
                                    TaskRepository taskRepository,
                                    TaskBranchLinkRepository taskBranchLinkRepository,
                                    TaskCommitLinkRepository taskCommitLinkRepository,
                                    ProjectMemberRepository projectMemberRepository) {
        this.gitHubClient = gitHubClient;
        this.gitRepoRepository = gitRepoRepository;
        this.branchRepository = branchRepository;
        this.commitRepository = commitRepository;
        this.taskRepository = taskRepository;
        this.taskBranchLinkRepository = taskBranchLinkRepository;
        this.taskCommitLinkRepository = taskCommitLinkRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public void syncRepositoryHistory(ProjectRepositoryEntity repository, String owner, String repo, String accessToken) {
        List<GitHubBranchResponse> branches = gitHubClient.getBranches(owner, repo, accessToken);

        log.info("Starting initial GitHub repository sync. repositoryId={}, repository={}, branches={}",
                repository.getId(),
                repository.getRepoName(),
                branches.size()
        );

        for (GitHubBranchResponse branchResponse : branches) {
            if (branchResponse == null || branchResponse.name() == null || branchResponse.name().isBlank()) {
                continue;
            }

            log.info("Syncing branch. repositoryId={}, repository={}, branch={}",
                    repository.getId(),
                    repository.getRepoName(),
                    branchResponse.name()
            );

            List<GitHubCommitResponse> commits = gitHubClient.getCommits(
                    owner,
                    repo,
                    branchResponse.name(),
                    INITIAL_SYNC_COMMITS_PER_BRANCH,
                    accessToken
            );

            syncBranchCommits(repository, branchResponse.name(), commits);
        }

        log.info(
                "Completed initial GitHub repository sync. repositoryId={}, repository={}, branches={}",
                repository.getId(),
                repository.getRepoName(),
                branches.size()
        );
    }

    public void syncPushEvent(GithubPushEvent event) {
        if (event.repository() == null || event.repository().id() == null) {
            log.warn("Ignored GitHub push event without repository id. ref={}", event.ref());
            return;
        }

        String branchName = extractBranchName(event.ref());
        if (branchName == null) {
            log.info("Ignored GitHub push event for unsupported ref. ref={}", event.ref());
            return;
        }

        Optional<ProjectRepositoryEntity> repositoryOptional = gitRepoRepository.findById(event.repository().id());
        if (repositoryOptional.isEmpty()) {
            log.warn("Ignored GitHub push event for unregistered repository. repositoryId={}", event.repository().id());
            return;
        }

        if (event.deleted()) {
            log.info(
                    "Received GitHub push delete event. repositoryId={}, repository={}, branch={}",
                    event.repository().id(),
                    event.repository().full_name(),
                    branchName
            );
            return;
        }

        List<ImportedCommit> importedCommits = (event.commits() != null ? event.commits() : List.<GithubPushEvent.Commit>of()).stream()
                .filter(commit -> commit != null && commit.id() != null && !commit.id().isBlank())
                .map(commit -> new ImportedCommit(
                        commit.id(),
                        defaultString(commit.message()),
                        resolveAuthorName(commit, event),
                        resolveCommittedAt(commit.timestamp())
                ))
                .toList();

        // 실시간 push는 "새로 들어온 commit"이므로 branch-linked task들에 commit을 자동 연동한다.
        syncImportedBranchCommits(repositoryOptional.get(), branchName, importedCommits, true);

        log.info(
                "Received GitHub push event. repositoryId={}, repository={}, ref={}, commits={}, forced={}",
                event.repository().id(),
                event.repository().full_name(),
                event.ref(),
                importedCommits.size(),
                event.forced()
        );
    }

    @Transactional
    public void syncBranchCommits(ProjectRepositoryEntity repository,
                                  String branchName,
                                  List<GitHubCommitResponse> commits) {
        List<ImportedCommit> importedCommits = commits.stream()
                .filter(commit -> commit != null && commit.sha() != null && !commit.sha().isBlank())
                .map(commit -> new ImportedCommit(
                        commit.sha(),
                        commit.commit() != null ? defaultString(commit.commit().message()) : "",
                        commit.commit() != null && commit.commit().author() != null ? commit.commit().author().name() : null,
                        commit.commit() != null && commit.commit().author() != null
                                ? resolveCommittedAt(commit.commit().author().date())
                                : LocalDateTime.now()
                ))
                .toList();

        // 초기 이력 동기화는 과거 commit까지 한꺼번에 가져오므로 task 자동 연동은 막는다.
        syncImportedBranchCommits(repository, branchName, importedCommits, false);
    }

    @Transactional
    void syncImportedBranchCommits(ProjectRepositoryEntity repository,
                                   String branchName,
                                   List<ImportedCommit> commits,
                                   boolean autoLinkTasks) {
        BranchEntity branch = branchRepository.findByRepositoryIdAndBranchName(repository.getId(), branchName)
                .orElseGet(() -> branchRepository.save(BranchEntity.builder()
                        .repository(repository)
                        .branchName(branchName)
                        .merged(false)
                        .build()));

        // push 경로에서만 현재 branch에 연결된 task-branch 링크를 읽어 온다.
        // 초기 sync에서는 빈 리스트를 써서 자동 연동 로직이 전혀 돌지 않게 한다.
        List<TaskBranchLinkEntity> taskBranchLinks = autoLinkTasks
                ? taskBranchLinkRepository.findByBranchId(branch.getId())
                : List.of();
        // task-commit 링크의 linkedBy는 null일 수 없으므로, 자동 링크의 작성자로 쓸 프로젝트 owner를 찾는다.
        ProjectMemberEntity autoLinkActor = taskBranchLinks.isEmpty()
                ? null
                : resolveAutoLinkActor(repository.getProject().getId());
        // 기존 규칙인 branch명 기반 task 추정은 그대로 유지한다.
        Optional<TaskEntity> branchTask = findTaskForBranch(repository.getProject().getId(), branchName);
        branchTask.ifPresent(task -> linkBranch(task, branch));

        for (ImportedCommit importedCommit : commits) {
            // commit은 task와 연결되기 전에 먼저 git_commits 테이블에 존재해야 하므로 upsert한다.
            CommitEntity commit = commitRepository.findById(importedCommit.sha())
                    .orElseGet(() -> commitRepository.save(CommitEntity.builder()
                            .sha(importedCommit.sha())
                            .branch(branch)
                            .message(importedCommit.message())
                            .authorName(importedCommit.authorName())
                            .committedAt(importedCommit.committedAt())
                            .build()));

            // 이번에 추가한 핵심 로직이다.
            // 이미 branch에 연결되어 있던 모든 task에 대해 현재 commit을 task-commit 링크로 복제한다.
            if (!taskBranchLinks.isEmpty() && autoLinkActor != null) {
                autoLinkCommitToTasks(taskBranchLinks, commit, autoLinkActor);
            }

            // 여기 아래의 기존 자동 추정 로직도 linkedBy가 필요하므로 owner를 못 찾으면 건너뛴다.
            if (autoLinkActor == null) {
                continue;
            }

            // branch명 기반 추정 task와 commit message의 #123 기반 추정 task를 합친다.
            // Set을 쓰는 이유는 같은 task가 두 경로로 들어와도 한 번만 링크하기 위해서다.
            Set<TaskEntity> tasksToLink = new LinkedHashSet<>();
            branchTask.ifPresent(tasksToLink::add);
            tasksToLink.addAll(findTasksFromCommitMessage(repository.getProject().getId(), importedCommit.message()));

            for (TaskEntity task : tasksToLink) {
                linkCommit(task, commit, autoLinkActor);
            }
        }
    }

    private ProjectMemberEntity resolveAutoLinkActor(Long projectId) {
        // 자동 링크는 실제 로그인 요청자가 없기 때문에 프로젝트 owner를 시스템 대리인처럼 사용한다.
        return projectMemberRepository.findByProjectIdAndRoleAndActiveTrue(projectId, ProjectRole.OWNER)
                .orElse(null);
    }

    private void autoLinkCommitToTasks(List<TaskBranchLinkEntity> taskBranchLinks,
                                       CommitEntity commit,
                                       ProjectMemberEntity autoLinkActor) {
        // 한 branch가 여러 task에 연결될 수 있으므로 branch 링크 수만큼 task-commit 링크를 만든다.
        for (TaskBranchLinkEntity taskBranchLink : taskBranchLinks) {
            Long taskId = taskBranchLink.getTask().getId();
            // GitHub webhook 재전송이나 중복 push 처리에도 동일 링크가 두 번 생기지 않게 막는다.
            if (taskCommitLinkRepository.existsByTaskIdAndCommitSha(taskId, commit.getSha())) {
                continue;
            }

            // branch에 연결되어 있던 task를 기준으로 현재 commit을 자동 연동한다.
            taskCommitLinkRepository.save(TaskCommitLinkEntity.builder()
                    .task(taskBranchLink.getTask())
                    .commit(commit)
                    .linkedBy(autoLinkActor)
                    // 자동으로 보이기만 한 상태이므로 "완료 근거로 승인됨" 상태는 아니다.
                    .accepted(false)
                    // 수동 링크와 구분되도록 생성 사유를 남긴다.
                    .comment("Auto linked from branch push")
                    .build());
        }
    }

    private Optional<TaskEntity> findTaskForBranch(Long projectId, String branchName) {
        Matcher matcher = BRANCH_TASK_ID_PATTERN.matcher(branchName);
        if (!matcher.find()) {
            return Optional.empty();
        }

        Long taskId = Long.parseLong(matcher.group(1));
        return taskRepository.findById(taskId)
                .filter(task -> Objects.equals(task.getProject().getId(), projectId));
    }

    private List<TaskEntity> findTasksFromCommitMessage(Long projectId, String message) {
        if (message == null || message.isBlank()) {
            return List.of();
        }

        Set<Long> taskIds = new LinkedHashSet<>();
        Matcher matcher = COMMIT_TASK_ID_PATTERN.matcher(message);
        while (matcher.find()) {
            taskIds.add(Long.parseLong(matcher.group(1)));
        }

        if (taskIds.isEmpty()) {
            return List.of();
        }

        return taskRepository.findAllById(taskIds).stream()
                .filter(task -> Objects.equals(task.getProject().getId(), projectId))
                .toList();
    }

    private void linkBranch(TaskEntity task, BranchEntity branch) {
        if (taskBranchLinkRepository.existsByTaskIdAndBranchId(task.getId(), branch.getId())) {
            return;
        }

        taskBranchLinkRepository.save(TaskBranchLinkEntity.builder()
                .task(task)
                .branch(branch)
                .primary(Boolean.TRUE)
                .note("Auto-linked from GitHub branch name")
                .build());
    }

    private void linkCommit(TaskEntity task, CommitEntity commit, ProjectMemberEntity linkedBy) {
        if (taskCommitLinkRepository.existsByTaskIdAndCommitSha(task.getId(), commit.getSha())) {
            return;
        }

        taskCommitLinkRepository.save(TaskCommitLinkEntity.builder()
                .task(task)
                .commit(commit)
                .linkedBy(linkedBy)
                .accepted(false)
                .comment("Auto-linked from GitHub push event")
                .build());
    }

    private String extractBranchName(String ref) {
        if (ref == null || !ref.startsWith("refs/heads/")) {
            return null;
        }
        return ref.substring("refs/heads/".length());
    }

    private String resolveAuthorName(GithubPushEvent.Commit commitEvent, GithubPushEvent pushEvent) {
        if (commitEvent.author() != null && commitEvent.author().name() != null && !commitEvent.author().name().isBlank()) {
            return commitEvent.author().name();
        }
        if (pushEvent.pusher() != null && pushEvent.pusher().name() != null && !pushEvent.pusher().name().isBlank()) {
            return pushEvent.pusher().name();
        }
        return null;
    }

    private LocalDateTime resolveCommittedAt(OffsetDateTime timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }

    public record ImportedCommit(
            String sha,
            String message,
            String authorName,
            LocalDateTime committedAt
    ) {
    }
}
