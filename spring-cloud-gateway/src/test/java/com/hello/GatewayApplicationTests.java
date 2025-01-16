package com.hello;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
		// todo tip io.netty.util.ResourceLeakDetector.reportUntracedLeak need breakPoints,
		//  The memory leak can be reproduced in about a minute or so
		// todo com.hello.filter.RequestBodyLogGlobalFilter.requestBodyStoreToExchange serverRequest.bodyToMono(byte[].class) netty leak? when throw LimitedDataBufferList.raiseLimitException
		// io.netty.util.ResourceLeakDetector.reportUntracedLeak will print the memory leak log、
		// invoke track->serverRequest.bodyToMono(byte[].class)
		// -> AbstractDataBufferDecoder.decodeToMono()  -> DataBufferUtils.join(input, this.maxInMemorySize) throws LimitedDataBufferList
		RestTemplate restTemplate = new RestTemplate();
		while (true) {
			try {
				Thread.sleep(10);
				String requestBody = "hello";
				String forObject = restTemplate.postForObject("http://localhost:8888/producer/hello", requestBody, String.class);
			} catch (Exception exception) {
				log.info("exception"+exception.getMessage());
			}
		}
	}

}
