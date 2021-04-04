package com.example.oauth2springreactiveexemple;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableConfigurationProperties(value = Application.MicroserviceCoreInformation.class)
@ComponentScan(basePackages = "com.example")
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		
		SpringApplication app = new SpringApplication(Application.class);
		app.setWebApplicationType(WebApplicationType.REACTIVE);
		app.setBannerMode(Mode.CONSOLE);
		SpringApplication.run(Application.class, args);
	}
	
	@Getter @Setter
	@ConfigurationProperties(prefix = "microservice.core")
	public static class MicroserviceCoreInformation {
		private String supplierUri;
	}
	
	@Bean
	public WebClient webClient(MicroserviceCoreInformation info) {
		
		log.info(" > Supplier Api uri : {}.", info.getSupplierUri());
		
		return WebClient.
				builder().
				uriBuilderFactory(new DefaultUriBuilderFactory(info.getSupplierUri())).
				build();
	}
}
