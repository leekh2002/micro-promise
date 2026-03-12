package com.gyuhyuk.micro_promise.data.dto.webhook;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class CreateWebhookRequest {

    private String name;
    private boolean active;
    private List<String> events;
    private Map<String, Object> config;
}