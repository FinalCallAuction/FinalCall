package com.finalcall.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients // Enable Feign clients
@EnableScheduling
public class FinalCallPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinalCallPaymentServiceApplication.class, args);
	}

}
