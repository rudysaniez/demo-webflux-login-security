package com.example.oauth2springreactiveexemple.controllers;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ApiBaseController {
    private ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
    private String currentAccessToken;

    public ApiBaseController(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
    }

    public Mono<String> getAccessToken(ServerWebExchange exchange) {
        Mono<String> result = ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication instanceof OAuth2AuthenticationToken)
                .cast(OAuth2AuthenticationToken.class)
                .flatMap(token -> authorizedClientRepository
                        .loadAuthorizedClient(token.getAuthorizedClientRegistrationId(), token, exchange)
                        .cast(OAuth2AuthorizedClient.class))
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map((accessToken) -> new String().concat(accessToken.getTokenValue()))
                .defaultIfEmpty(new String());
            //innerAccessToken.subscribe(value -> this.currentAccessToken = value);
            return result;
    }
}
