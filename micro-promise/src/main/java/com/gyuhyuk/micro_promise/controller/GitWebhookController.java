package com.gyuhyuk.micro_promise.controller;

import com.gyuhyuk.micro_promise.service.GitWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/github")
public class GitWebhookController {

    private final GitWebhookService gitWebhookService;

    public GitWebhookController(GitWebhookService gitWebhookService) {
        this.gitWebhookService = gitWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String rawBody
    ) {
        gitWebhookService.handle(event, rawBody);
        return ResponseEntity.ok().build();
    }
}
