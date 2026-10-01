package com.assetly.common;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.of(Map.of(
                "status", "UP",
                "service", "assetly-backend",
                "checkedAt", Instant.now().toString()
        ));
    }
}
