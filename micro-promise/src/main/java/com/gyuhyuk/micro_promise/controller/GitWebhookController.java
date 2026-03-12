package com.gyuhyuk.micro_promise.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/github")
public class GitWebhookController {

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody Map<String, Object> payload
    ) {

        switch (event) {
            case "create":
                if(payload.get("ref_type").equals("branch"))
                    System.out.println("Branch created: " + payload.get("ref"));
                break;

            case "delete":
                if(payload.get("ref_type").equals("branch"))
                    System.out.println("Branch deleted: " + payload.get("ref"));
                break;

            case "push":
                break;
        }

        return ResponseEntity.ok().build();
    }
}