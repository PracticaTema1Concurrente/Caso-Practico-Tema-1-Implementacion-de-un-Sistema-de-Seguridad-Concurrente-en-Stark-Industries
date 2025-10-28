package com.starkindustries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.starkindustries")
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
