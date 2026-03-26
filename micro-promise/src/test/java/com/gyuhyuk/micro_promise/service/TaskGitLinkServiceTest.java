package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.entity.BranchEntity;
import com.gyuhyuk.micro_promise.data.entity.CommitEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRepositoryEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.TaskEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskRole;
import com.gyuhyuk.micro_promise.data.entity.TaskStatus;
import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import com.gyuhyuk.micro_promise.repository.BranchRepository;
import com.gyuhyuk.micro_promise.repository.CommitRepository;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.TaskAssigneeRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskGitLinkServiceTest {

    @InjectMocks
    private TaskGitLinkService taskGitLinkService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private CommitRepository commitRepository;

    @Mock
    private TaskBranchLinkRepository taskBranchLinkRepository;

    @Mock
    private TaskCommitLinkRepository taskCommitLinkRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;

    @Test
    void linkBranch_projectOwnerCanLink() {
        TaskEntity task = createTask(1L);
        BranchEntity branch = createBranch(10L, 1L);
        ProjectMemberEntity projectOwner = createProjectMember(1L, "owner", ProjectRole.OWNER);

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(branchRepository.findById(10L)).willReturn(Optional.of(branch));
        given(projectMemberRepository.findByProjectIdAndUserUsernameAndActiveTrue(1L, "owner"))
                .willReturn(Optional.of(projectOwner));
        given(taskBranchLinkRepository.existsByTaskIdAndBranchId(1L, 10L)).willReturn(false);

        taskGitLinkService.linkBranch(1L, 10L, "owner");

        verify(taskBranchLinkRepository).save(any());
        verify(taskAssigneeRepository, never()).findRoleByTaskIdAndProjectMemberUserUsername(any(), any());
    }

    @Test
    void linkCommit_taskOwnerCanLink() {
        TaskEntity task = createTask(1L);
        CommitEntity commit = createCommit("abc123", 1L);
        ProjectMemberEntity taskOwner = createProjectMember(1L, "task-owner", ProjectRole.MEMBER);

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(commitRepository.findById("abc123")).willReturn(Optional.of(commit));
        given(projectMemberRepository.findByProjectIdAndUserUsernameAndActiveTrue(1L, "task-owner"))
                .willReturn(Optional.of(taskOwner));
        given(taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(1L, "task-owner"))
                .willReturn(TaskRole.OWNER);
        given(taskCommitLinkRepository.existsByTaskIdAndCommitSha(1L, "abc123")).willReturn(false);

        taskGitLinkService.linkCommit(1L, "abc123", "task-owner");

        verify(taskCommitLinkRepository).save(any());
    }

    @Test
    void linkCommit_nonOwnerAndNonTaskOwnerCannotLink() {
        TaskEntity task = createTask(1L);
        CommitEntity commit = createCommit("abc123", 1L);
        ProjectMemberEntity member = createProjectMember(1L, "member", ProjectRole.MEMBER);

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(commitRepository.findById("abc123")).willReturn(Optional.of(commit));
        given(projectMemberRepository.findByProjectIdAndUserUsernameAndActiveTrue(1L, "member"))
                .willReturn(Optional.of(member));
        given(taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(1L, "member"))
                .willReturn(TaskRole.MEMBER);

        assertThrows(IllegalArgumentException.class, () -> taskGitLinkService.linkCommit(1L, "abc123", "member"));

        verify(taskCommitLinkRepository, never()).save(any());
    }

    private TaskEntity createTask(Long projectId) {
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
        ReflectionTestUtils.setField(task, "id", 1L);
        return task;
    }

    private BranchEntity createBranch(Long branchId, Long projectId) {
        ProjectEntity project = ProjectEntity.builder()
                .name("project")
                .description("desc")
                .build();
        ReflectionTestUtils.setField(project, "id", projectId);

        ProjectRepositoryEntity repository = ProjectRepositoryEntity.builder()
                .id(100L)
                .project(project)
                .repoName("repo")
                .repoUrl("https://github.com/owner/repo")
                .build();

        BranchEntity branch = BranchEntity.builder()
                .repository(repository)
                .branchName("feature/test")
                .merged(false)
                .build();
        ReflectionTestUtils.setField(branch, "id", branchId);
        return branch;
    }

    private CommitEntity createCommit(String sha, Long projectId) {
        BranchEntity branch = createBranch(10L, projectId);
        return CommitEntity.builder()
                .sha(sha)
                .branch(branch)
                .message("message")
                .authorName("author")
                .committedAt(LocalDateTime.now())
                .build();
    }

    private ProjectMemberEntity createProjectMember(Long projectId, String username, ProjectRole role) {
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
                .role(role)
                .active(true)
                .build();
    }
}
