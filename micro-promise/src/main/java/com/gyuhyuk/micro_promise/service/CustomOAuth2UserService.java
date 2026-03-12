package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.CustomOAuth2User;
import com.gyuhyuk.micro_promise.data.dto.GithubResponse;
import com.gyuhyuk.micro_promise.data.dto.GoogleResponse;
import com.gyuhyuk.micro_promise.data.dto.NaverResponse;
import com.gyuhyuk.micro_promise.data.dto.OAuth2Response;
import com.gyuhyuk.micro_promise.data.dto.UserDTO;
import com.gyuhyuk.micro_promise.data.entity.AuthProvider;
import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import com.gyuhyuk.micro_promise.data.entity.UserRole;
import com.gyuhyuk.micro_promise.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = resolveOAuth2Response(registrationId, oAuth2User);
        if (oAuth2Response == null) {
            return null;
        }

        AuthProvider authProvider = AuthProvider.valueOf(oAuth2Response.getProvider());
        String accessToken = userRequest.getAccessToken().getTokenValue();
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        UserEntity existData = userRepository.findByUsername(username);

        if (existData == null) {
            UserEntity userEntity = UserEntity.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .provider(authProvider)
                    .providerUserId(oAuth2Response.getProviderId())
                    .githubAccessToken(authProvider == AuthProvider.GITHUB ? accessToken : null)
                    .role(UserRole.ROLE_USER)
                    .build();

            userRepository.save(userEntity);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        }

        existData.updateOAuthInfo(
                oAuth2Response.getEmail(),
                oAuth2Response.getName(),
                oAuth2Response.getProviderId()
        );
        if (authProvider == AuthProvider.GITHUB) {
            existData.updateGithubAccessToken(accessToken);
        }

        userRepository.save(existData);

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(existData.getUsername());
        userDTO.setName(oAuth2Response.getName());
        userDTO.setRole(existData.getRole().toString());

        return new CustomOAuth2User(userDTO);
    }

    private OAuth2Response resolveOAuth2Response(String registrationId, OAuth2User oAuth2User) {
        if ("naver".equals(registrationId)) {
            return new NaverResponse(oAuth2User.getAttributes());
        }
        if ("google".equals(registrationId)) {
            return new GoogleResponse(oAuth2User.getAttributes());
        }
        if ("github".equals(registrationId)) {
            return new GithubResponse(oAuth2User.getAttributes());
        }

        return null;
    }
}
