package com.example.oauth2springreactiveexemple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.example")
@SpringBootApplication
public class Oauth2SpringReactiveExempleApplication {

	public static void main(String[] args) {
		SpringApplication.run(Oauth2SpringReactiveExempleApplication.class, args);
	}
}
