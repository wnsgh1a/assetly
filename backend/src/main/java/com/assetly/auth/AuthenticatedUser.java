package com.assetly.auth;

public record AuthenticatedUser(Long id, String email, String name) {
}
