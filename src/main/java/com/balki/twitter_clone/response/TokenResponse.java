package com.balki.twitter_clone.response;

import lombok.Data;

@Data
public class TokenResponse {

    private String accessToken;

    private String refreshToken;
}