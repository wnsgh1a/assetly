package com.assetly.config;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionEnvironmentValidator {

    private static final String EXAMPLE_JWT_SECRET = "change-this-secret-before-production";
    private final String jwtSecret;
    private final String databasePassword;

    public ProductionEnvironmentValidator(
            @Value("${assetly.jwt.secret}") String jwtSecret,
            @Value("${spring.datasource.password}") String databasePassword
    ) {
        this.jwtSecret = jwtSecret;
        this.databasePassword = databasePassword;
        validate();
    }

    private void validate() {
        if (jwtSecret.isBlank()
                || jwtSecret.equals(EXAMPLE_JWT_SECRET)
                || jwtSecret.startsWith("replace-with-")
                || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 48) {
            throw new IllegalStateException("운영 JWT_SECRET은 예시 값이 아닌 48바이트 이상의 비밀값이어야 합니다.");
        }
        if (databasePassword.isBlank()
                || databasePassword.equals("assetly")
                || databasePassword.startsWith("replace-with-")) {
            throw new IllegalStateException("운영 데이터베이스 비밀번호는 기본값이 아닌 값으로 설정해야 합니다.");
        }
    }
}
