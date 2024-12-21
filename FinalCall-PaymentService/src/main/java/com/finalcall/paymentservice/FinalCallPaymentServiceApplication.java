package com.finalcall.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableFeignClients // Enable Feign clients
@EnableScheduling

public class FinalCallPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinalCallPaymentServiceApplication.class, args);
	}

}
