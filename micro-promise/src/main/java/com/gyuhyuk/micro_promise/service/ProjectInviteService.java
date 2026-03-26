package com.gyuhyuk.micro_promise.service;

// 초대 수락 결과로 돌려줄 프로젝트 DTO를 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
// 초대를 수락하는 사용자 정보를 받기 위해 import한다.
import com.gyuhyuk.micro_promise.data.dto.UserDTO;
// 초대 코드를 발급할 대상 프로젝트 엔티티를 조회하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
// 프로젝트와 초대 코드를 연결해 저장하는 엔티티를 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.entity.ProjectInviteCodeEntity;
// 초대 수락 시 프로젝트 멤버 엔티티를 생성하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
// 프로젝트 멤버 역할 enum을 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
// 초대를 수락하는 실제 사용자 엔티티를 조회하기 위해 import한다.
import com.gyuhyuk.micro_promise.data.entity.UserEntity;
// 초대 코드 저장/조회용 repository를 사용하기 위해 import한다.
import com.gyuhyuk.micro_promise.repository.ProjectInviteCodeRepository;
// 프로젝트 멤버 검증과 저장에 사용하는 repository를 import한다.
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
// 프로젝트 존재 여부 확인과 프로젝트 조회에 사용하는 repository를 import한다.
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
// 사용자 조회에 사용하는 repository를 import한다.
import com.gyuhyuk.micro_promise.repository.UserRepository;
// UNIQUE 충돌 시 재시도하기 위해 DB 무결성 예외를 import한다.
import org.springframework.dao.DataIntegrityViolationException;
// 이 클래스를 Spring service bean으로 등록하기 위해 import한다.
import org.springframework.stereotype.Service;
// DB write 작업을 하나의 트랜잭션으로 묶기 위해 import한다.
import org.springframework.transaction.annotation.Transactional;

// 초대 코드의 랜덤 문자열을 만들 때 사용할 보안 난수 생성기를 import한다.
import java.security.SecureRandom;

// 프로젝트 초대 코드 생성/갱신/수락 관련 비즈니스 로직을 담당하는 서비스다.
@Service
public class ProjectInviteService {
    // 초대 코드 랜덤 문자열을 생성할 때 사용할 SecureRandom 인스턴스다.
    private static final SecureRandom RNG = new SecureRandom();
    // 혼동하기 쉬운 문자(I, L, O, U 등)를 제외한 Base32 스타일 문자 집합이다.
    private static final char[] ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    // 초대 코드 엔티티 저장과 코드 조회에 사용하는 repository다.
    private final ProjectInviteCodeRepository projectInviteCodeRepository;
    // 프로젝트 존재 여부 확인과 프로젝트 엔티티 조회에 사용하는 repository다.
    private final ProjectRepository projectRepository;
    // 프로젝트 멤버 권한 검증과 멤버 저장에 사용하는 repository다.
    private final ProjectMemberRepository projectMemberRepository;
    // 초대 수락 사용자 엔티티 조회에 사용하는 repository다.
    private final UserRepository userRepository;

    // 필요한 repository 의존성을 생성자 주입으로 받는다.
    public ProjectInviteService(ProjectInviteCodeRepository projectInviteCodeRepository,
                                ProjectRepository projectRepository,
                                ProjectMemberRepository projectMemberRepository,
                                UserRepository userRepository) {
        // 초대 코드 repository를 필드에 저장한다.
        this.projectInviteCodeRepository = projectInviteCodeRepository;
        // 프로젝트 repository를 필드에 저장한다.
        this.projectRepository = projectRepository;
        // 프로젝트 멤버 repository를 필드에 저장한다.
        this.projectMemberRepository = projectMemberRepository;
        // 사용자 repository를 필드에 저장한다.
        this.userRepository = userRepository;
    }

    // 지정한 길이만큼 랜덤 토큰 문자열을 만든다.
    public static String randomToken(int length) {
        // 결과를 담을 문자 배열을 준비한다.
        char[] out = new char[length];
        // length 길이만큼 반복하면서 각 자리를 랜덤 문자로 채운다.
        for (int i = 0; i < length; i++) {
            // ALPHABET에서 임의의 인덱스를 뽑아 현재 자리에 저장한다.
            out[i] = ALPHABET[RNG.nextInt(ALPHABET.length)];
        }
        // 완성된 문자 배열을 문자열로 바꿔 반환한다.
        return new String(out);
    }

    // 사람이 읽기 쉽게 하이픈을 끼워 넣은 문자열로 변환한다.
    public static String formatWithHyphen(String token, int groupSize) {
        // 하이픈이 추가될 것을 고려해 StringBuilder를 충분한 크기로 만든다.
        StringBuilder sb = new StringBuilder(token.length() + token.length() / groupSize);
        // 원본 토큰의 각 문자를 순회한다.
        for (int i = 0; i < token.length(); i++) {
            // groupSize 단위로 끊을 때마다 하이픈을 넣는다.
            if (i > 0 && i % groupSize == 0) {
                sb.append('-');
            }
            // 현재 문자를 결과 문자열에 추가한다.
            sb.append(token.charAt(i));
        }
        // 하이픈이 삽입된 문자열을 반환한다.
        return sb.toString();
    }

    // 초대 코드 생성은 DB 저장이 포함되므로 트랜잭션으로 묶는다.
    @Transactional
    public String generateInviteCode(Long projectId) {
        // 랜덤 토큰의 길이를 12자로 고정한다.
        final int tokenLen = 12;
        // 충돌 시 재시도할 최대 횟수를 5회로 제한한다.
        final int maxRetry = 5;
        // 초대 코드를 발급할 프로젝트를 조회한다.
        ProjectEntity entity = projectRepository.findById(projectId)
                // 프로젝트가 없으면 잘못된 요청이므로 예외를 던진다.
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));

        // 코드 충돌이 날 수 있으므로 최대 maxRetry번까지 반복 시도한다.
        for (int attempt = 0; attempt < maxRetry; attempt++) {
            // 랜덤 토큰을 새로 만든다.
            String token = randomToken(tokenLen);
            // 최종 초대 코드를 "PRJ-{projectId}-{token}" 형식으로 만든다.
            String code = "PRJ-" + projectId + "-" + token;

            try {
                // 저장할 초대 코드 엔티티를 만든다.
                ProjectInviteCodeEntity projectInviteCodeEntity = ProjectInviteCodeEntity.builder()
                        // 생성한 초대 코드 문자열을 저장한다.
                        .code(code)
                        // 어떤 프로젝트의 초대 코드인지 연결한다.
                        .project(entity)
                        // builder로 엔티티 생성을 마친다.
                        .build();

                // 초대 코드 엔티티를 DB에 저장한다.
                projectInviteCodeRepository.save(projectInviteCodeEntity);
                // 저장이 성공하면 생성된 코드를 바로 반환한다.
                return code;
            } catch (DataIntegrityViolationException e) {
                // code 컬럼의 UNIQUE 제약에 걸렸다면 랜덤 충돌이므로 다음 루프로 재시도한다.
            }
        }

        // maxRetry번 모두 충돌했다면 서버 상태 문제로 보고 예외를 던진다.
        throw new IllegalStateException("Failed to generate unique invite code");
    }

    // 요청자 권한까지 검증하는 초대 코드 생성 메서드다.
    @Transactional
    public String generateInviteCode(Long projectId, String requesterUsername) {
        // 대상 프로젝트가 실제로 존재하는지 확인한다.
        if (!projectRepository.existsById(projectId)) {
            // 프로젝트가 없으면 초대 코드를 만들 수 없으므로 예외를 던진다.
            throw new IllegalArgumentException("Invalid project ID");
        }

        // 요청자가 해당 프로젝트의 멤버인지 확인한다.
        if (!projectMemberRepository.existsByProjectIdAndUserUsername(projectId, requesterUsername)) {
            // 프로젝트 멤버가 아니면 초대 코드 생성 권한이 없다.
            throw new IllegalArgumentException("User is not a member of the project");
        }

        // 요청자의 프로젝트 역할이 OWNER인지 확인한다.
        if (projectMemberRepository.findRoleByProjectIdAndUserUsername(projectId, requesterUsername) != ProjectRole.OWNER) {
            // owner만 초대 코드를 만들 수 있도록 제한한다.
            throw new IllegalArgumentException("Only project owners can generate invite codes");
        }

        // 권한 검증이 끝났으면 실제 코드 생성 로직을 재사용한다.
        return generateInviteCode(projectId);
    }

    // 초대 코드 갱신도 내부적으로는 새 코드를 다시 생성하는 것이므로 트랜잭션으로 묶는다.
    @Transactional
    public String updateInviteCode(Long projectId) {
        // 현재 구현은 기존 코드를 폐기 표시하지 않고 새 코드를 다시 발급한다.
        return generateInviteCode(projectId);
    }

    // 초대 코드를 사용해 프로젝트에 참여시키는 로직이다.
    public ProjectDTO acceptProjectInvite(UserDTO user, String inviteCode) {
        // 전달받은 초대 코드 문자열로 초대 코드 엔티티를 찾는다.
        ProjectInviteCodeEntity invite = projectInviteCodeRepository.findByCode(inviteCode)
                // 코드가 없으면 유효하지 않은 초대 코드이므로 예외를 던진다.
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        // 반환할 프로젝트 DTO 객체를 준비한다.
        ProjectDTO projectDTO = new ProjectDTO();
        // 초대를 수락하는 사용자 엔티티를 username으로 조회한다.
        UserEntity userEntity = userRepository.findByUsername(user.getUsername());

        // 프로젝트 멤버 엔티티를 만들어 현재 사용자를 MEMBER로 프로젝트에 추가할 준비를 한다.
        ProjectMemberEntity member = ProjectMemberEntity.builder()
                // 초대 코드가 가리키는 프로젝트를 연결한다.
                .project(invite.getProject())
                // 초대를 수락한 사용자 엔티티를 연결한다.
                .user(userEntity)
                // 초대로 들어오는 사용자는 기본적으로 MEMBER 역할을 가진다.
                .role(ProjectRole.MEMBER)
                // 가입 즉시 활성 멤버로 표시한다.
                .active(true)
                // builder로 엔티티 생성을 마친다.
                .build();

        // 응답 DTO에 프로젝트 id를 채운다.
        projectDTO.setId(invite.getProject().getId());
        // 응답 DTO에 프로젝트 이름을 채운다.
        projectDTO.setName(invite.getProject().getName());
        // 응답 DTO에 프로젝트 설명을 채운다.
        projectDTO.setDescription(invite.getProject().getDescription());

        // 새 프로젝트 멤버 엔티티를 DB에 저장한다.
        projectMemberRepository.save(member);

        // 참여가 완료된 프로젝트 정보를 DTO로 반환한다.
        return projectDTO;
    }
}
