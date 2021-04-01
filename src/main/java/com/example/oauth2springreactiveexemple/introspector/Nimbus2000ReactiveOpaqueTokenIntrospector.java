package com.example.oauth2springreactiveexemple.introspector;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Audience;

import reactor.core.publisher.Mono;

/**
 * This overloading is necessary for using the OAuth2 filter in the {@link WebClient webClient} building.
 * Indeed, the Ping-Connect, Adeo OIDC services implementation, doesn't provide the subject or "sub" in the JWT payload.
 * This is temporary, i waiting that Ping-Connect implemented this feature.
 * @author rudysaniez
 */
public class Nimbus2000ReactiveOpaqueTokenIntrospector extends NimbusReactiveOpaqueTokenIntrospector {

	private final URI introspectionUri;
	private final WebClient webClient;
	private String authorityPrefix = "SCOPE_";
	
	public Nimbus2000ReactiveOpaqueTokenIntrospector(String introspectionUri, String clientId, String clientSecret) {
		super(introspectionUri, clientId, clientSecret);
		
		this.introspectionUri = URI.create(introspectionUri);
		this.webClient = WebClient.builder().defaultHeaders((h) -> h.setBasicAuth(clientId, clientSecret)).build();
	}
	
	@Override
	public Mono<OAuth2AuthenticatedPrincipal> introspect(String token) {
		// @formatter:off
		return Mono.just(token)
				.flatMap(this::makeRequest)
				.flatMap(this::adaptToNimbusResponse)
				.map(this::parseNimbusResponse)
				.map(this::castToNimbusSuccess)
				.doOnNext((response) -> validate(token, response))
				.map(this::convertClaimsSet)
				.onErrorMap((e) -> !(e instanceof OAuth2IntrospectionException), this::onError);
		// @formatter:on
	}
	
	private Mono<ClientResponse> makeRequest(String token) {
		// @formatter:off
		return this.webClient.post()
				.uri(this.introspectionUri)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.body(BodyInserters.fromFormData("token", token))
				.exchange();
		// @formatter:on
	}
	
	private Mono<HTTPResponse> adaptToNimbusResponse(ClientResponse responseEntity) {
		HTTPResponse response = new HTTPResponse(responseEntity.rawStatusCode());
		response.setHeader(HttpHeaders.CONTENT_TYPE, responseEntity.headers().contentType().get().toString());
		if (response.getStatusCode() != HTTPResponse.SC_OK) {
			// @formatter:off
			return responseEntity.bodyToFlux(DataBuffer.class)
					.map(DataBufferUtils::release)
					.then(Mono.error(new OAuth2IntrospectionException(
							"Introspection endpoint responded with " + response.getStatusCode()))
					);
			// @formatter:on
		}
		return responseEntity.bodyToMono(String.class).doOnNext(response::setContent).map((body) -> response);
	}
	
	private TokenIntrospectionResponse parseNimbusResponse(HTTPResponse response) {
		try {
			return TokenIntrospectionResponse.parse(response);
		}
		catch (Exception ex) {
			throw new OAuth2IntrospectionException(ex.getMessage(), ex);
		}
	}
	
	private TokenIntrospectionSuccessResponse castToNimbusSuccess(TokenIntrospectionResponse introspectionResponse) {
		if (!introspectionResponse.indicatesSuccess()) {
			throw new OAuth2IntrospectionException("Token introspection failed");
		}
		return (TokenIntrospectionSuccessResponse) introspectionResponse;
	}
	
	private void validate(String token, TokenIntrospectionSuccessResponse response) {
		// relying solely on the authorization server to validate this token (not checking
		// 'exp', for example)
		if (!response.isActive()) {
			throw new BadOpaqueTokenException("Provided token isn't active");
		}
	}
	
	private OAuth2AuthenticatedPrincipal convertClaimsSet(TokenIntrospectionSuccessResponse response) {
		
		Map<String, Object> claims = response.toJSONObject();
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		if (response.getAudience() != null) {
			List<String> audiences = new ArrayList<>();
			for (Audience audience : response.getAudience()) {
				audiences.add(audience.getValue());
			}
			claims.put(OAuth2IntrospectionClaimNames.AUDIENCE, Collections.unmodifiableList(audiences));
		}
		if (response.getClientID() != null) {
			claims.put(OAuth2IntrospectionClaimNames.CLIENT_ID, response.getClientID().getValue());
		}
		if (response.getExpirationTime() != null) {
			Instant exp = response.getExpirationTime().toInstant();
			claims.put(OAuth2IntrospectionClaimNames.EXPIRES_AT, exp);
		}
		if (response.getIssueTime() != null) {
			Instant iat = response.getIssueTime().toInstant();
			claims.put(OAuth2IntrospectionClaimNames.ISSUED_AT, iat);
		}
		if (response.getIssuer() != null) {
			claims.put(OAuth2IntrospectionClaimNames.ISSUER, issuer(response.getIssuer().getValue()));
		}
		if (response.getNotBeforeTime() != null) {
			claims.put(OAuth2IntrospectionClaimNames.NOT_BEFORE, response.getNotBeforeTime().toInstant());
		}
		if (response.getScope() != null) {
			List<String> scopes = Collections.unmodifiableList(response.getScope().toStringList());
			claims.put(OAuth2IntrospectionClaimNames.SCOPE, scopes);

			for (String scope : scopes) {
				authorities.add(new SimpleGrantedAuthority(this.authorityPrefix + scope));
			}
		}
		
		//FIXME : Temporary code.
		claims.put(OAuth2IntrospectionClaimNames.SUBJECT, "anonymous_user");
		
		return new OAuth2IntrospectionAuthenticatedPrincipal(claims, authorities);
	}
	
	private URL issuer(String uri) {
		try {
			return new URL(uri);
		}
		catch (Exception ex) {
			throw new OAuth2IntrospectionException(
					"Invalid " + OAuth2IntrospectionClaimNames.ISSUER + " value: " + uri);
		}
	}
	
	private OAuth2IntrospectionException onError(Throwable ex) {
		return new OAuth2IntrospectionException(ex.getMessage(), ex);
	}
}
