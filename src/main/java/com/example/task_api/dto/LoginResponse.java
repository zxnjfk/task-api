package com.example.task_api.dto;

public class LoginResponse {

    private final String accessToken;
    private final String tokenType;

    public LoginResponse(String accessToken) {
        if(accessToken == null || accessToken.isBlank()){
            throw new IllegalArgumentException(
                "accessToken must not be blank"
            );
        }
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
    }

    public String getAccessToken(){
        return accessToken;
    }

    public String getTokenType(){
        return tokenType;
    }
}
