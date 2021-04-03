package com.example.oauth2springreactiveexemple.controller;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.example.oauth2springreactiveexemple.Api;
import com.example.oauth2springreactiveexemple.service.security.JwtService;
import com.example.supplier.model.Supplier;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class SupplierController {

	private final JwtService jwtService;
	private final WebClient webClient;
	
	public SupplierController(JwtService jwtService, WebClient webClient) {
		this.jwtService = jwtService;
		this.webClient = webClient;
	}
	
	/**
	 * @param exchange
	 * @return {@link Supplier suppliers}
	 */
	@GetMapping(value = "/api/suppliers", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<List<Supplier>>> getSuppliers(ServerWebExchange exchange) {
		
		log.info(" > Get suppliers.");
		
		return jwtService.getAccessToken(exchange).
				map(auth -> auth.getAccess_token()).
				flatMap(jwt -> webClient.get().
								uri(uri -> uri.pathSegment(Api.SUPPLIER_PATH).build()).
								accept(MediaType.APPLICATION_JSON).
								header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt).
								retrieve().
								bodyToMono(new ParameterizedTypeReference<List<Supplier>>() {})
				).
				map(listOfSuppliers -> ResponseEntity.ok(listOfSuppliers)).
				log();
	}
}
