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
    // Commit message 안의 "#123" 형태를 task id로 해석한다.
    private static final Pattern COMMIT_TASK_ID_PATTERN = Pattern.compile("#(\\d+)");
    // Branch name 안의 "task-123" 형태를 task id로 해석한다.
    private static final Pattern BRANCH_TASK_ID_PATTERN = Pattern.compile("(?:^|[/-])task-(\\d+)(?:$|[-/])");
    private static final int INITIAL_SYNC_COMMITS_PER_BRANCH = 100;

    private final GitHubClient gitHubClient;
    private final GitRepoRepository gitRepoRepository;
    private final BranchRepository branchRepository;
    private final CommitRepository commitRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskBranchLinkRepository taskBranchLinkRepository;
    private final TaskCommitLinkRepository taskCommitLinkRepository;

    public GitRepositorySyncService(GitHubClient gitHubClient,
                                    GitRepoRepository gitRepoRepository,
                                    BranchRepository branchRepository,
                                    CommitRepository commitRepository,
                                    TaskRepository taskRepository,
                                    ProjectMemberRepository projectMemberRepository,
                                    TaskBranchLinkRepository taskBranchLinkRepository,
                                    TaskCommitLinkRepository taskCommitLinkRepository) {
        this.gitHubClient = gitHubClient;
        this.gitRepoRepository = gitRepoRepository;
        this.branchRepository = branchRepository;
        this.commitRepository = commitRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskBranchLinkRepository = taskBranchLinkRepository;
        this.taskCommitLinkRepository = taskCommitLinkRepository;
    }

    public void syncRepositoryHistory(ProjectRepositoryEntity repository, String owner, String repo, String accessToken) {
        List<GitHubBranchResponse> branches = gitHubClient.getBranches(owner, repo, accessToken);

        for (GitHubBranchResponse branchResponse : branches) {
            if (branchResponse == null || branchResponse.name() == null || branchResponse.name().isBlank()) {
                continue;
            }

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

        syncImportedBranchCommits(repositoryOptional.get(), branchName, importedCommits);

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

        syncImportedBranchCommits(repository, branchName, importedCommits);
    }

    @Transactional
    void syncImportedBranchCommits(ProjectRepositoryEntity repository,
                                   String branchName,
                                   List<ImportedCommit> commits) {
        BranchEntity branch = branchRepository.findByRepositoryIdAndBranchName(repository.getId(), branchName)
                .orElseGet(() -> branchRepository.save(BranchEntity.builder()
                        .repository(repository)
                        .branchName(branchName)
                        .merged(false)
                        .build()));

        Optional<ProjectMemberEntity> ownerMember = projectMemberRepository.findByProjectIdAndRoleAndActiveTrue(
                repository.getProject().getId(),
                ProjectRole.OWNER
        );

        Optional<TaskEntity> branchTask = findTaskForBranch(repository.getProject().getId(), branchName);
        branchTask.ifPresent(task -> linkBranch(task, branch));

        for (ImportedCommit importedCommit : commits) {
            CommitEntity commit = commitRepository.findById(importedCommit.sha())
                    .orElseGet(() -> commitRepository.save(CommitEntity.builder()
                            .sha(importedCommit.sha())
                            .branch(branch)
                            .message(importedCommit.message())
                            .authorName(importedCommit.authorName())
                            .committedAt(importedCommit.committedAt())
                            .build()));

            if (ownerMember.isEmpty()) {
                continue;
            }

            // branch명 기반 연결과 commit message 기반 연결을 합쳐 한 번에 처리한다.
            Set<TaskEntity> tasksToLink = new LinkedHashSet<>();
            branchTask.ifPresent(tasksToLink::add);
            tasksToLink.addAll(findTasksFromCommitMessage(repository.getProject().getId(), importedCommit.message()));

            for (TaskEntity task : tasksToLink) {
                linkCommit(task, commit, ownerMember.get());
            }
        }
    }

    private String extractBranchName(String ref) {
        if (ref == null || !ref.startsWith("refs/heads/")) {
            return null;
        }
        return ref.substring("refs/heads/".length());
    }

    private Optional<TaskEntity> findTaskForBranch(Long projectId, String branchName) {
        // branch naming convention을 따르는 경우에만 자동 연결한다.
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

        // 다른 프로젝트 task로 잘못 연결되지 않도록 project id를 다시 검증한다.
        return taskRepository.findAllById(taskIds).stream()
                .filter(task -> Objects.equals(task.getProject().getId(), projectId))
                .toList();
    }

    private void linkBranch(TaskEntity task, BranchEntity branch) {
        // GitHub가 동일 이벤트를 재전송해도 중복 링크가 생기지 않게 막는다.
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
        // commit-task 링크도 동일하게 idempotent 하게 유지한다.
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
