package com.example.jammoney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JammoneyApplication {

	public static void main(String[] args) {
		SpringApplication.run(JammoneyApplication.class, args);
	}

}
