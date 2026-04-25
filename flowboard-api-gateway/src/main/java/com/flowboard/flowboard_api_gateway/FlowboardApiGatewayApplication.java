package com.flowboard.flowboard_api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlowboardApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowboardApiGatewayApplication.class, args);
		System.out.println("API-Gateway is Running.....!");
	}

}
