package com.flowboard.flowboard_api_gateway;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class FlowboardApiGatewayApplicationTests {

	@Test
	void mainShouldDelegateToSpringApplication() {
		try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
			FlowboardApiGatewayApplication.main(new String[]{"--test"});

			springApplication.verify(() -> SpringApplication.run(FlowboardApiGatewayApplication.class, new String[]{"--test"}));
		}
	}

}
