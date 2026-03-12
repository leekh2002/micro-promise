package com.gyuhyuk.micro_promise.data.dto.webhook;

import lombok.Getter;

@Getter
public class GithubWebhookConfig {

    private String content_type;
    private String insecure_ssl;
    private String url;

}