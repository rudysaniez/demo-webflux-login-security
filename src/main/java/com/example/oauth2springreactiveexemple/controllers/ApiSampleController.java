package com.example.oauth2springreactiveexemple.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/sample", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiSampleController extends ApiBaseController {

    public ApiSampleController(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        super(authorizedClientRepository);
    }

    @GetMapping(value="/whoami")
    public Mono<Map<String, Object>> index(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
        return oauth2User
                .map(OAuth2User::getAttributes);
    }

    @GetMapping("/whoami2")
    public Mono<String> whoami2(ServerWebExchange exchange) {
        return this.getAccessToken(exchange);
    }
    
    @GetMapping("/whoami3")
    public Mono<Map<String, Object>> whoami3(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
                                @AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        return oauth2User
                .map(OAuth2User::getAttributes);
    }
}
