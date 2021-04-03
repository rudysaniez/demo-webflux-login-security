package com.example.oauth2springreactiveexemple.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

import com.example.oauth2springreactiveexemple.Management;
import com.example.oauth2springreactiveexemple.introspector.Nimbus2000ReactiveOpaqueTokenIntrospector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) throws Exception {
    	
    	// @formatter:off
        http.
            oauth2Client().
            and().
            	csrf().disable().
            authorizeExchange().
            	matchers(EndpointRequest.to(Management.INFO, Management.HEALTH)).permitAll().
            	pathMatchers("/info").permitAll().
            anyExchange().
            	authenticated().
            and().
            	oauth2Login();
        
        return http.
        		build();
     // @formatter:on
    }
    
    @Bean
    ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    /**
	 * Using the nimbus2000 implementation. Indeed, if i want use the {@link WebClient webClient} with the OAuth2 filter, 
	 * i need overloading this implementation {@link NimbusReactiveOpaqueTokenIntrospector}. The Ping-Connect, OIDC services, doesn't
	 * return the subject or sub in the JWT payload. I get an exception, because the {@link DefaultOAuth2AuthenticatedPrincipal pincipalName}
	 * is null.
	 * @param registration
	 * @param introspectionUri
	 * @return {@link ReactiveOpaqueTokenIntrospector nimbus2000}
	 */
	@Bean
	public ReactiveOpaqueTokenIntrospector nimbus2000(ReactiveClientRegistrationRepository registration, 
			@Value("${spring.security.oauth2.resourceserver.opaquetoken.introspection-uri}") String introspectionUri) {
		
		ClientRegistration clientRegistration = registration.findByRegistrationId("adeo").block();
		
		log.info(" > introspectionUri={}, clientId={}", introspectionUri, clientRegistration.getClientId());
		
		return new Nimbus2000ReactiveOpaqueTokenIntrospector(introspectionUri, clientRegistration.getClientId(), 
				clientRegistration.getClientSecret());
	}
    
    /**
     * SecurityWebFilterChain with no security at all ! Activated with <b>nosecurity</b> profile
     */
    @Bean
    @Profile({"local", "dev"})
    SecurityWebFilterChain disable(ServerHttpSecurity http) {
        return http.cors().and()
                .csrf().disable()
                .authorizeExchange()
                .anyExchange().permitAll()
                .and().build();
    }

    @Bean
    public ForwardedHeaderTransformer forwardedHeaderTransformer() {
        return new ForwardedHeaderTransformer();
    }
}
