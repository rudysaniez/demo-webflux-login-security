package com.example.oauth2springreactiveexemple.controller;

import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.example.oauth2springreactiveexemple.controller.exception.UnauthorizeException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class AuthController {

    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
    private final ReactiveOpaqueTokenIntrospector nimbus;
    
    public AuthController(ServerOAuth2AuthorizedClientRepository authorizedClientRepository, ReactiveOpaqueTokenIntrospector nimbus) {
    	
        this.authorizedClientRepository = authorizedClientRepository;
        this.nimbus = nimbus;
    }

    /**
     * @param exchange
     * @param oauth2User
     * @return {@link Claims}
     */
    @GetMapping(value = "/claims", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Claims>> claims(ServerWebExchange exchange, @AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
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
    public Mono<ResponseEntity<Whoami>> whoami(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
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
     * @return {@link JwtService}
     */
    @GetMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Auth>> auth(ServerWebExchange exchange, @AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
    	// @formatter:off
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
        
        return Mono.zip(values -> aggregate((Auth)values[0], (OAuth2User)values[1]), auth, oauth2User).
        	map(a -> ResponseEntity.ok(a)).
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
    	return auth(exchange, oauth2User).
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
    * @param oauth2User
    * @return {@link Void void}, just the {@link HttpStatus http status} is important here.
    */
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> info(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    	
        if(oauth2User == null) {
            throw new UnauthorizeException();
        } else {
            return Mono.just(ResponseEntity.ok().build());
        }
    }
    
    /**
     * @param auth
     * @param oauth2User
     * @return {@link Auth authentification information}
     */
    private Auth aggregate(Auth auth, OAuth2User oauth2User) {
    	
    	if(oauth2User != null && oauth2User.getAttributes() != null)
    		auth.setClaims(claims(oauth2User));
    	
    	return auth;
    }
    
    /**
     * @param oauth2User
     * @return {@link Claims}
     */
    private Claims claims(OAuth2User oauth2User) {
    	
    	// @formatter:off
    	return Claims.builder().
			email(oauth2User.getAttribute("email")).
			ldap(oauth2User.getAttribute("sub")).
			name(oauth2User.getAttribute("name")).
			preferredlanguage(oauth2User.getAttribute("preferredlanguage")).
			departmentnumber(oauth2User.getAttribute("departmentnumber")).
			privbusinesscategorycode(oauth2User.getAttribute("privbusinesscategorycode")).
			build();
    	// @formatter:on
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Auth {
    	
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
    	private String privbusinesscategorycode;
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