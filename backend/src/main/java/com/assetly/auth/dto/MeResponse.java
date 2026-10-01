package com.assetly.auth.dto;

import com.assetly.auth.AuthenticatedUser;

public record MeResponse(Long id, String email, String name) {

    public static MeResponse from(AuthenticatedUser user) {
        return new MeResponse(user.id(), user.email(), user.name());
    }
}
