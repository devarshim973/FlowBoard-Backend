package com.flowboard.flowboard_server;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlowboardServerApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainShouldDelegateToSpringApplication() {
		try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
			FlowboardServerApplication.main(new String[]{"--test"});

			springApplication.verify(() -> SpringApplication.run(FlowboardServerApplication.class, new String[]{"--test"}));
		}
	}

}
