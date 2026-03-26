package com.gyuhyuk.micro_promise.service;

// task assignee 요청 DTO를 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.dto.TaskAssigneeDTO;
// task 생성/조회/수정 응답 DTO를 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.dto.TaskDTO;
// 프로젝트 멤버 엔티티를 조회 결과로 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
// task-assignee 연결 엔티티를 생성하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.entity.TaskAssigneeEntity;
// task 엔티티를 생성하고 수정하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.entity.TaskEntity;
// task assignee 역할 enum을 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.entity.TaskRole;
// task 상태 enum을 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.entity.TaskStatus;
// 프로젝트 멤버 조회용 repository를 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
// 프로젝트 존재 여부와 프로젝트 엔티티 조회를 위해 import한다.
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
// task-assignee 저장 및 권한 검증용 repository를 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.repository.TaskAssigneeRepository;
// task 저장/조회/삭제용 repository를 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.repository.TaskRepository;
// 이 클래스를 Spring service bean으로 등록하기 위해 import한다.
import org.springframework.stereotype.Service;
// 여러 DB write를 하나의 트랜잭션으로 묶기 위해 import한다.
import org.springframework.transaction.annotation.Transactional;

// assignee 엔티티 목록을 담을 가변 리스트 구현체를 사용하기 위해 import한다.
import java.util.ArrayList;
// assignee 역할 매핑의 입력 순서를 유지하기 위해 import한다.
import java.util.LinkedHashMap;
// 컬렉션 타입으로 List를 사용하기 위해 import한다.
import java.util.List;
// username -> role 매핑을 만들기 위해 import한다.
import java.util.Map;
// null 비교와 id 비교를 안전하게 하기 위해 import한다.
import java.util.Objects;
// stream 결과를 리스트/맵으로 수집하기 위해 import한다.
import java.util.stream.Collectors;

// task 관련 비즈니스 로직을 담당하는 서비스임을 나타낸다.
@Service
public class TaskService {
    // task 자체를 저장/조회/삭제하는 repository다.
    private final TaskRepository taskRepository;
    // 프로젝트 존재 여부 확인과 프로젝트 엔티티 조회에 사용하는 repository다.
    private final ProjectRepository projectRepository;
    // 프로젝트 멤버 검증 및 assignee 멤버 조회에 사용하는 repository다.
    private final ProjectMemberRepository projectMemberRepository;
    // task assignee 저장 및 task owner 권한 확인에 사용하는 repository다.
    private final TaskAssigneeRepository taskAssigneeRepository;

    // 필요한 repository 의존성을 생성자 주입으로 받는다.
    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       ProjectMemberRepository projectMemberRepository,
                       TaskAssigneeRepository taskAssigneeRepository) {
        // task repository를 필드에 저장한다.
        this.taskRepository = taskRepository;
        // project repository를 필드에 저장한다.
        this.projectRepository = projectRepository;
        // project member repository를 필드에 저장한다.
        this.projectMemberRepository = projectMemberRepository;
        // task assignee repository를 필드에 저장한다.
        this.taskAssigneeRepository = taskAssigneeRepository;
    }

    // task 생성은 task와 assignee가 함께 저장되므로 하나의 트랜잭션으로 묶는다.
    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO, Long parentTaskId, Long projectId, String requester) {
        // task를 만들 대상 프로젝트가 실제로 존재하는지 먼저 확인한다.
        if (!projectRepository.existsById(projectId)) {
            // 프로젝트가 없으면 생성 자체를 중단한다.
            throw new IllegalArgumentException("Project does not exist");
        }

        // 요청자가 해당 프로젝트의 멤버인지 확인한다.
        if (!projectMemberRepository.existsByProjectIdAndUserUsername(projectId, requester)) {
            // 프로젝트 멤버가 아니면 task 생성 권한이 없다고 보고 중단한다.
            throw new IllegalArgumentException("User is not a member of the project");
        }

        // assignees가 null일 수도 있으므로 null이면 빈 리스트로 치환한다.
        List<TaskAssigneeDTO> requestedAssignees = taskDTO.getAssignees() != null ? taskDTO.getAssignees() : List.of();
        // 요청된 assignee DTO들에서 username만 추출한다.
        List<String> assigneeUsernames = requestedAssignees.stream()
                // DTO에서 assignee username을 꺼낸다.
                .map(TaskAssigneeDTO::getAssigneeName)
                // null username은 잘못된 입력이므로 조회 대상에서 제외한다.
                .filter(Objects::nonNull)
                // 최종적으로 username 리스트를 만든다.
                .toList();

        // assignee가 하나도 없으면 DB를 조회하지 않고 빈 리스트를 사용한다.
        List<ProjectMemberEntity> projectMembers = assigneeUsernames.isEmpty()
                ? List.of()
                // assignee username들이 현재 프로젝트의 실제 멤버인지 한 번에 조회한다.
                : projectMemberRepository.findProjectMembersByProjectIdAndUsernameIn(projectId, assigneeUsernames);

        // 요청한 assignee 수와 실제 프로젝트 멤버로 조회된 수가 다르면 잘못된 assignee가 섞여 있다는 뜻이다.
        if (projectMembers.size() != assigneeUsernames.size()) {
            // 존재하지 않거나 비활성/타 프로젝트 멤버인 assignee가 있으면 생성하지 않는다.
            throw new IllegalArgumentException("All assignees must be active project members");
        }

        // parentTaskId가 없으면 루트 task이므로 parent는 null이다.
        TaskEntity parentEntity = parentTaskId == null
                ? null
                // parentTaskId가 있으면 실제 부모 task를 조회한다.
                : taskRepository.findById(parentTaskId)
                // 부모 task가 없으면 잘못된 요청으로 간주한다.
                .orElseThrow(() -> new IllegalArgumentException("Parent task does not exist"));

        // 부모 task가 존재하면, 현재 생성 대상 프로젝트와 같은 프로젝트 소속인지 검증한다.
        if (parentEntity != null && !Objects.equals(parentEntity.getProject().getId(), projectId)) {
            // 다른 프로젝트의 task를 부모로 연결하는 것을 막는다.
            throw new IllegalArgumentException("Parent task must belong to the same project");
        }

        // 저장할 task 엔티티를 구성한다.
        TaskEntity taskEntity = TaskEntity.builder()
                // 제목을 요청 DTO에서 가져온다.
                .title(taskDTO.getTitle())
                // 설명을 요청 DTO에서 가져온다.
                .description(taskDTO.getDescription())
                // 문자열 상태를 enum으로 변환한다.
                .status(TaskStatus.valueOf(taskDTO.getStatus()))
                // 프로젝트 엔티티를 조회해서 연결한다.
                .project(projectRepository.findById(projectId)
                        // 위에서 existsById로 이미 확인했지만, 안전하게 한 번 더 보장한다.
                        .orElseThrow(() -> new IllegalArgumentException("Project does not exist")))
                // 새 task의 진행률은 항상 0으로 시작한다.
                .progress(0)
                // 같은 부모 아래에서의 정렬 순서를 저장한다.
                .orderIndex(taskDTO.getOrderIndex())
                // 부모 task가 있으면 연결하고, 없으면 null을 저장한다.
                .parent(parentEntity)
                // builder로 만든 엔티티를 완성한다.
                .build();

        // task 엔티티를 DB에 먼저 저장한다.
        taskRepository.save(taskEntity);

        // 저장할 task-assignee 엔티티들을 담을 리스트를 만든다.
        List<TaskAssigneeEntity> assignees = new ArrayList<>();
        // 요청 DTO의 assignee 목록을 username -> role 형태의 맵으로 바꾼다.
        Map<String, String> roleMap = requestedAssignees.stream()
                // username을 key로, role을 value로 수집한다.
                .collect(Collectors.toMap(
                        // key는 assignee username이다.
                        TaskAssigneeDTO::getAssigneeName,
                        // value는 assignee 역할 문자열이다.
                        TaskAssigneeDTO::getRole,
                        // username 중복이 들어오면 마지막 값을 사용한다.
                        (left, right) -> right,
                        // 입력 순서를 유지하는 LinkedHashMap을 사용한다.
                        LinkedHashMap::new
                ));

        // 실제 프로젝트 멤버 엔티티들을 순회하면서 task-assignee 엔티티를 만든다.
        for (ProjectMemberEntity projectMember : projectMembers) {
            // 현재 프로젝트 멤버의 username을 꺼낸다.
            String username = projectMember.getUser().getUsername();
            // 해당 username에 대해 요청에서 지정한 역할을 찾는다.
            String role = roleMap.get(username);

            // task와 project member를 연결하는 assignee 엔티티를 만든다.
            TaskAssigneeEntity taskAssigneeEntity = TaskAssigneeEntity.builder()
                    // 어떤 task에 대한 assignee인지 연결한다.
                    .task(taskEntity)
                    // 어떤 프로젝트 멤버가 assignee인지 연결한다.
                    .projectMember(projectMember)
                    // 문자열 역할을 enum으로 변환해 저장한다.
                    .role(TaskRole.valueOf(role))
                    // builder로 만든 엔티티를 완성한다.
                    .build();

            // 완성된 assignee 엔티티를 저장 대상 리스트에 추가한다.
            assignees.add(taskAssigneeEntity);
        }

        // 모든 assignee 엔티티를 한 번에 저장한다.
        taskAssigneeRepository.saveAll(assignees);
        // 저장된 task와 assignee 목록을 응답 DTO로 변환해 반환한다.
        return TaskDTO.fromEntity(taskEntity, assignees);
    }

    // 특정 프로젝트에 속한 task 목록을 DTO로 조회한다.
    List<TaskDTO> getTasksByProjectId(Long projectId) {
        // 프로젝트 id로 task 엔티티 목록을 조회한다.
        List<TaskEntity> taskEntities = taskRepository.findByProjectId(projectId);
        // 엔티티 목록을 DTO 목록으로 변환해 반환한다.
        return taskEntities.stream()
                // 각 task 엔티티를 DTO로 변환한다.
                .map(TaskDTO::fromEntity)
                // 리스트로 수집한다.
                .collect(Collectors.toList());
    }

    // 특정 사용자가 assignee로 연결된 task 목록을 DTO로 조회한다.
    List<TaskDTO> getTasksByAssignee(String username) {
        // username으로 해당 사용자가 할당된 task 엔티티들을 조회한다.
        List<TaskEntity> taskEntities = taskAssigneeRepository.findTaskByAssigneeUsername(username);
        // 엔티티 목록을 DTO 목록으로 변환해 반환한다.
        return taskEntities.stream()
                // 각 task 엔티티를 DTO로 변환한다.
                .map(TaskDTO::fromEntity)
                // 리스트로 수집한다.
                .collect(Collectors.toList());
    }

    // task 수정은 엔티티 변경이 일어나므로 트랜잭션으로 묶는다.
    @Transactional
    TaskDTO updateTask(TaskDTO taskDTO, String requester) {
        // 수정 대상 task를 id로 조회한다.
        TaskEntity taskEntity = taskRepository.findById(taskDTO.getId())
                // task가 없으면 수정할 수 없으므로 예외를 던진다.
                .orElseThrow(() -> new IllegalArgumentException("Task does not exist"));

        // 요청자가 해당 task의 OWNER인지 확인한다.
        if (taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskDTO.getId(), requester) != TaskRole.OWNER) {
            // owner가 아니면 수정 권한이 없으므로 중단한다.
            throw new IllegalArgumentException("User cannot update this task");
        }

        // 수정할 필드만 담은 임시 TaskEntity를 만들어 기존 엔티티에 반영한다.
        taskEntity.updateTaskInfo(TaskEntity.builder()
                // 새로운 제목을 넣는다.
                .title(taskDTO.getTitle())
                // 새로운 설명을 넣는다.
                .description(taskDTO.getDescription())
                // 새로운 상태를 enum으로 변환해 넣는다.
                .status(TaskStatus.valueOf(taskDTO.getStatus()))
                // 새로운 진행률을 넣는다.
                .progress(taskDTO.getProgress())
                // 새로운 순서를 넣는다.
                .orderIndex(taskDTO.getOrderIndex())
                // builder 객체를 완성한다.
                .build());

        // 수정된 task 엔티티를 DTO로 변환해 반환한다.
        return TaskDTO.fromEntity(taskEntity);
    }

    // task 삭제도 DB write이므로 트랜잭션으로 묶는다.
    @Transactional
    void deleteTask(Long taskId, String requester) {
        // 삭제 대상 task가 실제로 존재하는지 먼저 확인한다.
        if (!taskRepository.existsById(taskId)) {
            // task가 없으면 삭제할 수 없으므로 예외를 던진다.
            throw new IllegalArgumentException("Task does not exist");
        }

        // 요청자가 해당 task의 OWNER인지 확인한다.
        if (taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskId, requester) != TaskRole.OWNER) {
            // owner가 아니면 삭제 권한이 없으므로 중단한다.
            throw new IllegalArgumentException("User cannot delete this task");
        }
        // 검증이 끝났으면 task를 삭제한다.
        taskRepository.deleteById(taskId);
    }
}
