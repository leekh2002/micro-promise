package com.gyuhyuk.micro_promise.controller;

import com.gyuhyuk.micro_promise.jwt.TokenProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Test
    void authorize_shouldReturnTokenAndHeader() throws Exception {
        AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        Mockito.when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(authentication);
        Mockito.when(tokenProvider.createToken(authentication)).thenReturn("test-jwt-token");

        String loginJson = "{\"username\":\"testuser\",\"password\":\"testpass\"}";

        mockMvc.perform(post("/api/authenticate")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer test-jwt-token"))
                .andExpect(jsonPath("$.token").value("test-jwt-token"));
    }
}