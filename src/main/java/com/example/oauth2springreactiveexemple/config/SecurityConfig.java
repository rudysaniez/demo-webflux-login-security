package com.example.oauth2springreactiveexemple.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

@EnableWebFluxSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) throws Exception {
        http
                .oauth2Client()
                //.and().oauth2ResourceServer()
                .and().csrf()
                .disable()
                .authorizeExchange()
                    .and().authorizeExchange().pathMatchers("/login/me").permitAll()
                    .anyExchange().authenticated()
                    .and().oauth2Login()
        ;
        return http.build();
    }
    @Bean
    ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
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
