package com.gyuhyuk.micro_promise.data.dto.webhook;

import lombok.Getter;

@Getter
public class GithubWebhookLastResponse {

    private Integer code;
    private String status;
    private String message;

}