package com.example.oauth2springreactiveexemple.controllers;

import com.example.oauth2springreactiveexemple.controllers.exceptions.UnauthorizeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AuthController {

    private static final String NSREDIRECT = "NSREDIRECT";

        private ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

        public AuthController(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
            this.authorizedClientRepository = authorizedClientRepository;
        }

        @GetMapping("/login/me2")
        public Mono<String> index(ServerWebExchange exchange) {
        	
            Mono<String> result = ReactiveSecurityContextHolder
                    .getContext()
                    .map(SecurityContext::getAuthentication)
                    .log()
                    .filter(authentication -> authentication instanceof OAuth2AuthenticationToken)
                    .cast(OAuth2AuthenticationToken.class)
                    .flatMap(token -> authorizedClientRepository
                            .loadAuthorizedClient(token.getAuthorizedClientRegistrationId(), token, exchange)
                            .cast(OAuth2AuthorizedClient.class))
                    .map(OAuth2AuthorizedClient::getAccessToken)
                    .log()
                    .map((accessToken) -> new String().concat(accessToken.getTokenValue()))
                    .defaultIfEmpty(new String());
            return result;
        }

    /**
     * Allows recover informations user
     *
     * @param oauth2UserMono
     * @return informations if user authenticated, or unauthorize exception
     */
    @GetMapping(value = {"/login/me"})
    public Mono<OidcUserInfo> me(@AuthenticationPrincipal Mono<DefaultOidcUser> oauth2UserMono) {
    	
    	
    	
        if (oauth2UserMono == null) {
            throw new UnauthorizeException();
        } else {
            return oauth2UserMono.map(DefaultOidcUser::getUserInfo);
        }
    }

    /**
     * Allows redirect with front after authentification
     *
     * @param nsRedirect
     * @param response
     * @return redirect with front
     */
    @GetMapping("/login/authorize")
    public ResponseEntity authorize(@CookieValue(NSREDIRECT) String nsRedirect, ServerHttpResponse response) {
    	
        ResponseCookie cookie = ResponseCookie.from(NSREDIRECT, "")
                .maxAge(0)
                .build();
        response.addCookie(cookie);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(nsRedirect))
                .build();
    }
}