package com.assetly.auth.dto;

import com.assetly.user.User;

public record AuthUserResponse(Long id, String email, String name) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(user.getId(), user.getEmail(), user.getName());
    }
}
