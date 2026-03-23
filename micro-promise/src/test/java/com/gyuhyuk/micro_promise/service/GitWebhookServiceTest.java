package com.gyuhyuk.micro_promise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubPushEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GitWebhookServiceTest {

    @InjectMocks
    private GitWebhookService gitWebhookService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private GitRepositorySyncService gitRepositorySyncService;

    @Test
    void handle_routesPushEventToSyncService() throws Exception {
        String payload = "{\"ref\":\"refs/heads/main\"}";
        GithubPushEvent event = new GithubPushEvent(
                "refs/heads/main",
                null,
                null,
                false,
                false,
                false,
                null,
                null,
                List.of(),
                null
        );

        given(objectMapper.readValue(payload, GithubPushEvent.class)).willReturn(event);

        gitWebhookService.handle("push", payload);

        verify(gitRepositorySyncService).syncPushEvent(event);
    }
}
