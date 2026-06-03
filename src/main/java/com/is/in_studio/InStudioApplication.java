package com.is.in_studio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InStudioApplication {

	public static void main(String[] args) {
		SpringApplication.run(InStudioApplication.class, args);
	}

}
