package com.flowboard.flowboard_api_gateway.security;

import org.springframework.stereotype.Component;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    public Predicate<String> isSecured = uri -> false;
}