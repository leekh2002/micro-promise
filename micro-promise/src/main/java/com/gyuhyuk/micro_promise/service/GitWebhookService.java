package com.gyuhyuk.micro_promise.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubCreateEvent;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubDeleteEvent;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubPingEvent;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubPushEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GitWebhookService {

    private static final Logger log = LoggerFactory.getLogger(GitWebhookService.class);

    private final ObjectMapper objectMapper;
    private final GitRepositorySyncService gitRepositorySyncService;

    public GitWebhookService(ObjectMapper objectMapper,
                             GitRepositorySyncService gitRepositorySyncService) {
        this.objectMapper = objectMapper;
        this.gitRepositorySyncService = gitRepositorySyncService;
    }

    public void handle(String event, String rawBody) {
        switch (event) {
            case "ping" -> handlePing(readValue(rawBody, GithubPingEvent.class));
            case "create" -> handleCreate(readValue(rawBody, GithubCreateEvent.class));
            case "delete" -> handleDelete(readValue(rawBody, GithubDeleteEvent.class));
            case "push" -> handlePush(readValue(rawBody, GithubPushEvent.class));
            default -> log.info("Ignored unsupported GitHub webhook event. event={}", event);
        }
    }

    private <T> T readValue(String rawBody, Class<T> targetType) {
        try {
            return objectMapper.readValue(rawBody, targetType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid GitHub webhook payload", e);
        }
    }

    public void handlePing(GithubPingEvent event) {
        log.info(
                "Received GitHub ping event. hookId={}, repositoryId={}, repository={}",
                event.hook_id(),
                event.repository() != null ? event.repository().id() : null,
                event.repository() != null ? event.repository().full_name() : null
        );
    }

    public void handleCreate(GithubCreateEvent event) {
        if (!"branch".equals(event.ref_type())) {
            log.info("Ignored GitHub create event. refType={}", event.ref_type());
            return;
        }

        log.info(
                "Received GitHub branch create event. repositoryId={}, repository={}, branch={}",
                event.repository() != null ? event.repository().id() : null,
                event.repository() != null ? event.repository().full_name() : null,
                event.ref()
        );
    }

    public void handleDelete(GithubDeleteEvent event) {
        if (!"branch".equals(event.ref_type())) {
            log.info("Ignored GitHub delete event. refType={}", event.ref_type());
            return;
        }

        log.info(
                "Received GitHub branch delete event. repositoryId={}, repository={}, branch={}",
                event.repository() != null ? event.repository().id() : null,
                event.repository() != null ? event.repository().full_name() : null,
                event.ref()
        );
    }

    public void handlePush(GithubPushEvent event) {
        // Reuse the same persistence rules for both webhook pushes and initial sync.
        gitRepositorySyncService.syncPushEvent(event);
    }
}
