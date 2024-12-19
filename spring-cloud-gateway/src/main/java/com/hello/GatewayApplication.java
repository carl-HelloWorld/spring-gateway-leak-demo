package com.hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@Slf4j
public class GatewayApplication {


	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
		System.out.println("启动完成");
		// todo tip io.netty.util.ResourceLeakDetector.reportUntracedLeak need breakPoints,
		//  The memory leak can be reproduced in about a minute or so
		// todo com.hello.filter.RequestBodyLogGlobalFilter.requestBodyStoreToExchange serverRequest.bodyToMono(byte[].class) netty leak? when throw LimitedDataBufferList.raiseLimitException
		// io.netty.util.ResourceLeakDetector.reportUntracedLeak will print the memory leak log、
		// invoke track->serverRequest.bodyToMono(byte[].class)
		// -> AbstractDataBufferDecoder.decodeToMono
		// -> ByteArrayDecoder
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
