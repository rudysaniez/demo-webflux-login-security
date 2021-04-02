package com.example.oauth2springreactiveexemple.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.example.oauth2springreactiveexemple.controllers.exceptions.UnauthorizeException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
    private ReactiveOpaqueTokenIntrospector nimbus;
    
    public AuthController(ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
    		ReactiveOpaqueTokenIntrospector nimbus) {
    	
        this.authorizedClientRepository = authorizedClientRepository;
        this.nimbus = nimbus;
    }

    /**
     * @param exchange
     * @param oauth2User
     * @return {@link Claims}
     */
    @GetMapping(value = "/claims", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Claims>> index(ServerWebExchange exchange, @AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
    	// @formatter:off
        return oauth2User.
                map(user -> claims(user)).
                map(claims -> ResponseEntity.ok(claims)).
                log();
        // @formatter:on
    }
    
    /**
     * @param oauth2User
     * @return {@link Whoami}
     */
    @GetMapping(value = "/whoami", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Whoami>> index(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
    	// @formatter:off
        return oauth2User
                .map(user -> Whoami.builder().ldap(user.getName()).build())
                .map(whoami -> ResponseEntity.ok(whoami))
                .log();
        // @formatter:on
    }
    
    /**
     * @param exchange
     * @param oauth2User
     * @return {@link Jwt}
     */
    @GetMapping(value = {"/identification", "/"})
    public Mono<ResponseEntity<Jwt>> identification(ServerWebExchange exchange, @AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
    	// @formatter:off
        Mono<Jwt> result = ReactiveSecurityContextHolder.
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
                map(accessToken -> Jwt.builder().access_token(accessToken).build()).
        		log();
        
        return Mono.zip(values -> aggregate((Jwt)values[0], (OAuth2User)values[1]), result, oauth2User).
        	map(jwt -> ResponseEntity.ok(jwt)).
        	log();
     // @formatter:on
    }
    
    /**
     * @param exchange
     * @param oauth2User
     * @return {@link Introspect}
     */
    @GetMapping(value = "/introspect", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Introspect>> introspect(ServerWebExchange exchange, @AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
    	// @formatter:off
    	return identification(exchange, oauth2User).
    		map(re -> re.getBody()).
    		flatMap(token -> nimbus.introspect(token.getAccess_token())).
    		log().
    		map(principal -> Introspect.
    							builder().
    							value(Boolean.TRUE).
    							build()).
			map(i -> ResponseEntity.ok(i)).
			log();
    	// @formatter:on
    }
    
    /**
     * Allows recover informations user
     *
     * @param oauth2UserMono
     * @return informations if user authenticated, or unauthorize exception
     */
    @GetMapping(value = "/info")
    public Mono<ResponseEntity<Void>> info(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
        if(oauth2User == null) {
            throw new UnauthorizeException();
        } else {
            return Mono.just(ResponseEntity.ok().build());
        }
    }
    
    /**
     * @param jwt
     * @param user
     * @return {@link Jwt}
     */
    private Jwt aggregate(Jwt jwt, OAuth2User user) {
    	
    	if(user != null && user.getAttributes() != null) {
    		
    		log.info(" > {}.", user.getAttributes().toString());
    		
    		jwt.setClaims(claims(user));
    	}
    	
    	return jwt;
    }
    
    /**
     * @param user
     * @return {@link Claims}
     */
    private Claims claims(OAuth2User user) {
    	
    	return Claims.builder().
			email(user.getAttribute("email")).
			ldap(user.getAttribute("sub")).
			name(user.getAttribute("name")).
			preferredlanguage(user.getAttribute("preferredlanguage")).
			departmentnumber(user.getAttribute("departmentnumber")).
			build();
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Jwt {
    	private String access_token;
    	private Claims claims;
    }
    
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Claims {
    	
    	private String name;
    	private String ldap;
    	private String email;
    	private String title;
    	private String preferredlanguage;
    	private String departmentnumber;
    }
    
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Whoami {
    	private String ldap;
    }
    
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Introspect {
    	private boolean value;
    }
}