package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.AcceptProjectInviteRequest;
import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
import com.gyuhyuk.micro_promise.data.dto.UserDTO;
import com.gyuhyuk.micro_promise.data.entity.*;
import com.gyuhyuk.micro_promise.repository.ProjectInviteCodeRepository;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import com.gyuhyuk.micro_promise.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class ProjectInviteService {
    private static final SecureRandom RNG = new SecureRandom();

    // Crockford Base32 (혼동 문자 제거)
    private static final char[] ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    private ProjectInviteCodeRepository projectInviteCodeRepository;
    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private UserRepository userRepository;

    public ProjectInviteService(ProjectInviteCodeRepository projectInviteCodeRepository, ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository, UserRepository userRepository) {
        this.projectInviteCodeRepository = projectInviteCodeRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    /** length: 랜덤 토큰 길이 (예: 10~12 추천) */
    public static String randomToken(int length) {
        char[] out = new char[length];
        for (int i = 0; i < length; i++) {
            out[i] = ALPHABET[RNG.nextInt(ALPHABET.length)];
        }
        return new String(out);
    }

    /** 보기 좋게 하이픈을 넣고 싶을 때 */
    public static String formatWithHyphen(String token, int groupSize) {
        StringBuilder sb = new StringBuilder(token.length() + token.length() / groupSize);
        for (int i = 0; i < token.length(); i++) {
            if (i > 0 && i % groupSize == 0) sb.append('-');
            sb.append(token.charAt(i));
        }
        return sb.toString();
    }

    @Transactional
    public String generateInviteCode(Long projectId) {
        final int tokenLen = 12;          // 추천
        final int maxRetry = 5;           // 현실적으로 2~5면 충분
        ProjectEntity entity = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));


        for (int attempt = 0; attempt < maxRetry; attempt++) {
            String token = randomToken(tokenLen);
            // 옵션: 가독성용 하이픈
            // token = InviteCodeGenerator.formatWithHyphen(token, 4); // "K7D9-3FQW-J2AB"

            String code = "PRJ-" + projectId + "-" + token;

            try {
                ProjectInviteCodeEntity projectInviteCodeEntity = ProjectInviteCodeEntity.builder()
                        .code(code)
                        .project(entity)
                        .build();

                projectInviteCodeRepository.save(projectInviteCodeEntity);
                return code;
            } catch (DataIntegrityViolationException e) {
                // code에 UNIQUE 제약이 걸려있다면, 충돌 시 여기로 들어옴 → 재시도
            }
        }

        throw new IllegalStateException("Failed to generate unique invite code");
    }

    @Transactional
    public String updateInviteCode(Long projectId) {
        return generateInviteCode(projectId);
    }

    public ProjectDTO acceptProjectInvite(UserDTO user, String inviteCode) {
        ProjectInviteCodeEntity invite = projectInviteCodeRepository.findByCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        ProjectDTO projectDTO = new ProjectDTO();
        UserEntity userEntity = userRepository.findByUsername(user.getUsername());

        ProjectMemberEntity member = ProjectMemberEntity.builder()
                .project(invite.getProject())
                .user(userEntity)
                .role(ProjectRole.MEMBER)
                .build();

        projectDTO.setId(invite.getProject().getId());
        projectDTO.setName(invite.getProject().getName());
        projectDTO.setDescription(invite.getProject().getDescription());

        projectMemberRepository.save(member);

        return projectDTO;
    }
}
