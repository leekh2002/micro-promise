package com.gyuhyuk.micro_promise.data.dto.webhook;

import lombok.Getter;

import java.util.List;

@Getter
public class GithubWebhookResponse {

    private String type;
    private Long id;
    private String name;
    private boolean active;

    private List<String> events;

    private GithubWebhookConfig config;

    private String updated_at;
    private String created_at;

    private String url;
    private String test_url;
    private String ping_url;
    private String deliveries_url;

    private GithubWebhookLastResponse last_response;

}
