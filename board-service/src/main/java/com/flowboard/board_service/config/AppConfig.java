package com.flowboard.board_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI();
    }

    @Bean
    public GlobalOpenApiCustomizer customizer() {
        return openApi -> openApi.getPaths().values()
                .forEach(pathItem ->
                        pathItem.readOperations().forEach(operation ->
                                operation.addParametersItem(
                                        new io.swagger.v3.oas.models.parameters.Parameter()
                                                .in("header")
                                                .required(true)
                                                .name("X-User-Id")
                                                .description("User Id Header")
                                )
                        )
                );
    }
}
