package com.assetly.auth.dto;

import com.assetly.user.User;

public record LoginResponse(String accessToken, AuthUserResponse user) {

    public static LoginResponse of(String accessToken, User user) {
        return new LoginResponse(accessToken, AuthUserResponse.from(user));
    }
}
