package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.entity.BranchEntity;
import com.gyuhyuk.micro_promise.data.entity.CommitEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.TaskBranchLinkEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskCommitLinkEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskRole;
import com.gyuhyuk.micro_promise.repository.BranchRepository;
import com.gyuhyuk.micro_promise.repository.CommitRepository;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.TaskAssigneeRepository;
import com.gyuhyuk.micro_promise.repository.TaskBranchLinkRepository;
import com.gyuhyuk.micro_promise.repository.TaskCommitLinkRepository;
import com.gyuhyuk.micro_promise.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class TaskGitLinkService {
    // task 본문과 소속 프로젝트를 조회하기 위한 repository다.
    private final TaskRepository taskRepository;
    // 사용자가 선택한 branch를 조회하기 위한 repository다.
    private final BranchRepository branchRepository;
    // 사용자가 선택한 commit을 조회하기 위한 repository다.
    private final CommitRepository commitRepository;
    // task-branch 링크를 저장하기 위한 repository다.
    private final TaskBranchLinkRepository taskBranchLinkRepository;
    // task-commit 링크를 저장하기 위한 repository다.
    private final TaskCommitLinkRepository taskCommitLinkRepository;
    // 프로젝트 멤버와 프로젝트 owner 여부를 조회하기 위한 repository다.
    private final ProjectMemberRepository projectMemberRepository;
    // task owner 여부를 조회하기 위한 repository다.
    private final TaskAssigneeRepository taskAssigneeRepository;

    // 필요한 저장소/권한 검사용 의존성을 생성자 주입으로 받는다.
    public TaskGitLinkService(TaskRepository taskRepository,
                              BranchRepository branchRepository,
                              CommitRepository commitRepository,
                              TaskBranchLinkRepository taskBranchLinkRepository,
                              TaskCommitLinkRepository taskCommitLinkRepository,
                              ProjectMemberRepository projectMemberRepository,
                              TaskAssigneeRepository taskAssigneeRepository) {
        // task 조회용 의존성을 저장한다.
        this.taskRepository = taskRepository;
        // branch 조회용 의존성을 저장한다.
        this.branchRepository = branchRepository;
        // commit 조회용 의존성을 저장한다.
        this.commitRepository = commitRepository;
        // task-branch 링크 저장용 의존성을 저장한다.
        this.taskBranchLinkRepository = taskBranchLinkRepository;
        // task-commit 링크 저장용 의존성을 저장한다.
        this.taskCommitLinkRepository = taskCommitLinkRepository;
        // 프로젝트 멤버/owner 검사용 의존성을 저장한다.
        this.projectMemberRepository = projectMemberRepository;
        // task owner 검사용 의존성을 저장한다.
        this.taskAssigneeRepository = taskAssigneeRepository;
    }

    // 사용자가 지정한 branch를 특정 task에 수동으로 연결한다.
    @Transactional
    public void linkBranch(Long taskId, Long branchId, String requesterUsername) {
        // 링크 대상 task가 실제로 존재하는지 확인한다.
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        // 사용자가 선택한 branch가 실제로 존재하는지 확인한다.
        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        // 다른 프로젝트의 branch를 현재 task에 잘못 연결하지 못하게 막는다.
        validateSameProject(task, branch.getRepository().getProject().getId());
        // 현재 요청자가 프로젝트 owner 또는 task owner인지 검증한다.
        authorize(task, requesterUsername);

        // 같은 task-branch 링크가 이미 있으면 중복 저장하지 않고 끝낸다.
        if (taskBranchLinkRepository.existsByTaskIdAndBranchId(taskId, branchId)) {
            return;
        }

        // 수동으로 연결된 branch 링크를 저장한다.
        taskBranchLinkRepository.save(TaskBranchLinkEntity.builder()
                // 어떤 task에 연결했는지 저장한다.
                .task(task)
                // 어떤 branch를 연결했는지 저장한다.
                .branch(branch)
                // 수동 연결은 기본값으로 primary가 아니라고 둔다.
                .primary(Boolean.FALSE)
                // 자동 추정이 아니라 사용자가 직접 연결했다는 메모를 남긴다.
                .note("Manually linked")
                .build());
    }

    // 사용자가 지정한 commit을 특정 task에 수동으로 연결한다.
    @Transactional
    public void linkCommit(Long taskId, String commitSha, String requesterUsername) {
        // 링크 대상 task 존재 여부를 먼저 검증한다.
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        // 지정한 sha의 commit이 실제로 저장돼 있는지 검증한다.
        CommitEntity commit = commitRepository.findById(commitSha)
                .orElseThrow(() -> new IllegalArgumentException("Commit not found"));

        // 다른 프로젝트의 commit을 현재 task에 연결하지 못하게 막는다.
        validateSameProject(task, commit.getBranch().getRepository().getProject().getId());
        // 권한 검증과 동시에 linkedBy에 기록할 프로젝트 멤버 엔티티를 받아온다.
        ProjectMemberEntity actor = authorize(task, requesterUsername);

        // 같은 task-commit 링크가 이미 있으면 중복 저장하지 않는다.
        if (taskCommitLinkRepository.existsByTaskIdAndCommitSha(taskId, commitSha)) {
            return;
        }

        // 수동 commit 링크를 저장한다.
        taskCommitLinkRepository.save(TaskCommitLinkEntity.builder()
                // 어떤 task와 연결되는지 기록한다.
                .task(task)
                // 어떤 commit과 연결되는지 기록한다.
                .commit(commit)
                // 누가 이 링크를 만들었는지 기록한다.
                .linkedBy(actor)
                // 연결 즉시 accepted로 보지 않고, 후속 승인 흐름 여지를 남긴다.
                .accepted(false)
                // 자동 링크와 구분되도록 수동 연결 메모를 남긴다.
                .comment("Manually linked")
                .build());
    }

    // task와 branch/commit이 같은 프로젝트 소속인지 검증한다.
    private void validateSameProject(TaskEntity task, Long targetProjectId) {
        // 프로젝트 id가 다르면 교차 프로젝트 오염이므로 예외를 던진다.
        if (!Objects.equals(task.getProject().getId(), targetProjectId)) {
            throw new IllegalArgumentException("Task and Git resource must belong to the same project");
        }
    }

    // 현재 사용자가 링크 생성 권한이 있는지 확인하고, 링크 생성 주체(ProjectMember)를 반환한다.
    private ProjectMemberEntity authorize(TaskEntity task, String requesterUsername) {
        // task가 속한 프로젝트 id를 기준으로 멤버십을 검사한다.
        Long projectId = task.getProject().getId();
        // 비활성 멤버는 링크 생성 권한이 없으므로 active 멤버만 조회한다.
        ProjectMemberEntity projectMember = projectMemberRepository
                .findByProjectIdAndUserUsernameAndActiveTrue(projectId, requesterUsername)
                .orElseThrow(() -> new IllegalArgumentException("User is not an active member of the project"));

        // 프로젝트 owner면 어떤 task든 링크할 수 있으므로 즉시 허용한다.
        if (projectMember.getRole() == ProjectRole.OWNER) {
            return projectMember;
        }

        // 프로젝트 owner가 아니라면 이 task의 task owner인지 추가로 검사한다.
        TaskRole taskRole = taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(task.getId(), requesterUsername);
        // task owner면 자기 task에 한해 링크를 허용한다.
        if (taskRole == TaskRole.OWNER) {
            return projectMember;
        }

        // 둘 다 아니면 권한이 없으므로 예외를 던진다.
        throw new IllegalArgumentException("Only project owners or task owners can link Git resources");
    }
}
