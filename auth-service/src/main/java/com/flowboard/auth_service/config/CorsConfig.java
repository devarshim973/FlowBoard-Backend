package com.flowboard.auth_service.config;

// CORS is handled at the API gateway for browser requests.
// Keeping another MVC CORS config here causes duplicate Access-Control-Allow-Origin headers.
public final class CorsConfig {
    private CorsConfig() {
    }
}
