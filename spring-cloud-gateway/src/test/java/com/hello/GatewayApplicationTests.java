package com.hello;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatewayApplication.class)
@Slf4j
public class GatewayApplicationTests {

	@BeforeEach
	public void setUp() {
		System.setProperty("spring.codec.max-in-memory-size","1");
		System.setProperty("io.netty.leakDetection.level","ADVANCED");
	}

	@Test
	public void contextLoads() {
		RestTemplate restTemplate = new RestTemplate();
		while (true) {
			try {
				String requestBody = "hello";
				String forObject = restTemplate.postForObject("http://localhost:8888/producer/hello", requestBody, String.class);
			} catch (Exception exception) {
				log.info("exception"+exception.getMessage());
			}
		}
	}

}
