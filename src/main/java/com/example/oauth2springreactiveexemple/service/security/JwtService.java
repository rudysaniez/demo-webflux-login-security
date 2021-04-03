package com.example.oauth2springreactiveexemple.service.security;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import com.example.oauth2springreactiveexemple.controller.AuthController;
import com.example.oauth2springreactiveexemple.controller.AuthController.Auth;
import com.example.oauth2springreactiveexemple.controller.exception.UnauthorizeException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class JwtService {

	private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
	
	public JwtService(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
		this.authorizedClientRepository = authorizedClientRepository;
	}
	
	/**
	 * @param exchange
	 * @return
	 */
	public Mono<AuthController.Auth> getAccessToken(ServerWebExchange exchange) {
		
		Mono<Auth> auth = ReactiveSecurityContextHolder.
                getContext().
                map(SecurityContext::getAuthentication).
                filter(authentication -> authentication instanceof OAuth2AuthenticationToken).
                cast(OAuth2AuthenticationToken.class).
                flatMap(token -> authorizedClientRepository.
                		loadAuthorizedClient(token.getAuthorizedClientRegistrationId(), token, exchange).
                		cast(OAuth2AuthorizedClient.class)).
                map(OAuth2AuthorizedClient::getAccessToken).
                map(accessToken -> accessToken.getTokenValue()).
                doOnNext(log::info).
                switchIfEmpty(Mono.error(new UnauthorizeException("The token is empty."))).
                map(accessToken -> Auth.builder().access_token(accessToken).build()).
        		log();
		
		return auth;
	}
}
