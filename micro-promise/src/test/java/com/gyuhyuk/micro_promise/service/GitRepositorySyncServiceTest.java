package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.webhook.GithubPushEvent;
import com.gyuhyuk.micro_promise.data.entity.BranchEntity;
import com.gyuhyuk.micro_promise.data.entity.CommitEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRepositoryEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.TaskBranchLinkEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskStatus;
import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import com.gyuhyuk.micro_promise.repository.BranchRepository;
import com.gyuhyuk.micro_promise.repository.CommitRepository;
import com.gyuhyuk.micro_promise.repository.GitRepoRepository;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.TaskBranchLinkRepository;
import com.gyuhyuk.micro_promise.repository.TaskCommitLinkRepository;
import com.gyuhyuk.micro_promise.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GitRepositorySyncServiceTest {

    @InjectMocks
    private GitRepositorySyncService gitRepositorySyncService;

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private GitRepoRepository gitRepoRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private CommitRepository commitRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskBranchLinkRepository taskBranchLinkRepository;

    @Mock
    private TaskCommitLinkRepository taskCommitLinkRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Test
    void syncPushEvent_autoLinksCommitToTasksLinkedToBranch() {
        ProjectRepositoryEntity repository = createRepository(100L, 1L);
        BranchEntity branch = createBranch(10L, repository);
        TaskEntity task = createTask(20L, 1L);
        TaskBranchLinkEntity taskBranchLink = TaskBranchLinkEntity.builder()
                .task(task)
                .branch(branch)
                .primary(Boolean.TRUE)
                .note("linked")
                .build();
        ProjectMemberEntity projectOwner = createProjectOwner(1L, "owner");

        GithubPushEvent event = new GithubPushEvent(
                "refs/heads/feature/test",
                "before",
                "after",
                false,
                false,
                false,
                new GithubPushEvent.Repository(100L, "repo", "owner/repo", "https://github.com/owner/repo"),
                new GithubPushEvent.Pusher("pusher", "pusher@test.com"),
                List.of(new GithubPushEvent.Commit(
                        "abc123",
                        "commit message",
                        OffsetDateTime.parse("2026-03-26T12:00:00+09:00"),
                        new GithubPushEvent.Author("author", "author@test.com", "author")
                )),
                null
        );

        given(gitRepoRepository.findById(100L)).willReturn(Optional.of(repository));
        given(branchRepository.findByRepositoryIdAndBranchName(100L, "feature/test")).willReturn(Optional.of(branch));
        given(taskBranchLinkRepository.findByBranchId(10L)).willReturn(List.of(taskBranchLink));
        given(projectMemberRepository.findByProjectIdAndRoleAndActiveTrue(1L, ProjectRole.OWNER))
                .willReturn(Optional.of(projectOwner));
        given(commitRepository.findById("abc123")).willReturn(Optional.empty());
        given(commitRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0, CommitEntity.class));
        given(taskCommitLinkRepository.existsByTaskIdAndCommitSha(20L, "abc123")).willReturn(false);

        gitRepositorySyncService.syncPushEvent(event);

        verify(taskCommitLinkRepository).save(argThat(link ->
                link.getTask().getId().equals(20L)
                        && link.getCommit().getSha().equals("abc123")
                        && link.getLinkedBy() == projectOwner
                        && !link.isAccepted()
                        && "Auto linked from branch push".equals(link.getComment())
        ));
    }

    @Test
    void syncBranchCommits_doesNotAutoLinkTaskCommit() {
        ProjectRepositoryEntity repository = createRepository(100L, 1L);
        BranchEntity branch = createBranch(10L, repository);
        CommitEntity commit = CommitEntity.builder()
                .sha("abc123")
                .branch(branch)
                .message("message")
                .authorName("author")
                .committedAt(LocalDateTime.now())
                .build();

        given(branchRepository.findByRepositoryIdAndBranchName(100L, "feature/test")).willReturn(Optional.of(branch));
        given(commitRepository.findById("abc123")).willReturn(Optional.of(commit));

        gitRepositorySyncService.syncImportedBranchCommits(
                repository,
                "feature/test",
                List.of(new GitRepositorySyncService.ImportedCommit(
                        "abc123",
                        "message",
                        "author",
                        LocalDateTime.now()
                )),
                false
        );

        verify(taskBranchLinkRepository, never()).findByBranchId(any());
        verify(taskCommitLinkRepository, never()).save(any());
    }

    private ProjectRepositoryEntity createRepository(Long repositoryId, Long projectId) {
        ProjectEntity project = ProjectEntity.builder()
                .name("project")
                .description("desc")
                .build();
        ReflectionTestUtils.setField(project, "id", projectId);

        return ProjectRepositoryEntity.builder()
                .id(repositoryId)
                .project(project)
                .repoName("repo")
                .repoUrl("https://github.com/owner/repo")
                .build();
    }

    private BranchEntity createBranch(Long branchId, ProjectRepositoryEntity repository) {
        BranchEntity branch = BranchEntity.builder()
                .repository(repository)
                .branchName("feature/test")
                .merged(false)
                .build();
        ReflectionTestUtils.setField(branch, "id", branchId);
        return branch;
    }

    private TaskEntity createTask(Long taskId, Long projectId) {
        ProjectEntity project = ProjectEntity.builder()
                .name("project")
                .description("desc")
                .build();
        ReflectionTestUtils.setField(project, "id", projectId);

        TaskEntity task = TaskEntity.builder()
                .project(project)
                .title("task")
                .description("desc")
                .status(TaskStatus.TODO)
                .progress(0)
                .orderIndex(0)
                .build();
        ReflectionTestUtils.setField(task, "id", taskId);
        return task;
    }

    private ProjectMemberEntity createProjectOwner(Long projectId, String username) {
        ProjectEntity project = ProjectEntity.builder()
                .name("project")
                .description("desc")
                .build();
        ReflectionTestUtils.setField(project, "id", projectId);

        UserEntity user = UserEntity.builder()
                .username(username)
                .email(username + "@test.com")
                .name(username)
                .build();

        return ProjectMemberEntity.builder()
                .project(project)
                .user(user)
                .role(ProjectRole.OWNER)
                .active(true)
                .build();
    }
}
