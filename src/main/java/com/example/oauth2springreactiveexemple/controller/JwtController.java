package com.example.oauth2springreactiveexemple.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.example.oauth2springreactiveexemple.service.security.JwtService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class JwtController {
	
	private final JwtService jwtService;
	
	public JwtController(JwtService jwtService) {
		this.jwtService = jwtService;
	}
	
	/**
	 * @param exchange
	 * @return JWT
	 */
	@GetMapping(value = "/api/jwt", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getJwt(ServerWebExchange exchange) {
		
		log.info(" > Get a JWT.");
		
		return jwtService.getAccessToken(exchange).
				map(auth -> auth.getAccess_token()).
				map(jwt -> ResponseEntity.ok(jwt)).
				log();
	}
}
