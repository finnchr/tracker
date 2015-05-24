package com.finnchristian.tracker.model.runkeeper;

import com.google.gson.annotations.SerializedName;

public class Token {
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("access_token")
    private String accessToken;

    public Token(final String tokenType, final String accessToken) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
