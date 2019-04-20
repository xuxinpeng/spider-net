package com.boundary.aaron.spider.recruit.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.boundary.aaron.spider.recruit" })
public class RecruitApplication {
	public static void main(String[] args) {
		SpringApplication.run(RecruitApplication.class, args);
	}
}