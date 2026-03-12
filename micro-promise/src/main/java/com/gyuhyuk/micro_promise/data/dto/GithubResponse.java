package com.gyuhyuk.micro_promise.data.dto;

import java.util.Map;

public class GithubResponse implements OAuth2Response {

    private final Map<String, Object> attributes;

    public GithubResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "GITHUB";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        Object email = attributes.get("email");
        return email == null ? null : email.toString();
    }

    @Override
    public String getName() {
        Object name = attributes.get("name");
        if (name != null && !name.toString().isBlank()) {
            return name.toString();
        }

        return attributes.get("login").toString();
    }
}
