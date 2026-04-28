package com.flowboard.flowboard_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class FlowboardServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowboardServerApplication.class, args);
		System.out.println("Eureka Server is Running....");
	}

}
