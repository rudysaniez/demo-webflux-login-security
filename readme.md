<p align="center">
	<a href="" rel="noopener">
	 <img width=200px height=200px src="spring-webflux.png" alt="Project logo">
 </a>
</p>

<h3 align="center">WebFlux and OAuth2</h3>

<div align="center">

  [![Status](https://img.shields.io/badge/status-active-success.svg)]()

</div>

---

For beginning, this link [here](https://www.baeldung.com/spring-oauth-login-webflux).  
It's the base of this development and present you the login identification with a authentification service, like with google by example. So start your engine 🏎

Good reading! 🌈

## 📝 Table of Contents
- [Presentation](#presentation)
- [Authentification](#authentification)
- [Endpoints](#endpoints)
	- [Info](#info)
	- [Auth](#auth)
	- [Claims](#claims)
	- [Whoami](#whoami)
	- [Introspect](#introspect)
---

## Presentation

This example show you how to make an authentification with `OAuth2`. Here, i use `Spring-WebFlux` and `Spring-Security`.

## Authentification

For the authentification configuration, Spring provides several properties that must be set :  

- client-id
- client-secret
- scope
- authorization-grant-type
- client-authentication-method

This properties are set for the **client registration**.  

**For the provider :**  

- authorization-uri
- token-uri
- user-info-uri
- user-name-attribute
- issuer-uri
- **jwk-set-uri**

The `JWKS` or too `JSON-Web-Key-Set` is a set of Keys containing the **public keys** used to verify any `JSON-Web-Token` (JWT) issued by the authorization server and signed using the `RS256` signing algorithm. 

Two algorithms are supported for signing JWTs : `RS256` and `HS256`.
The `RS256` generates an asymmetric signature, which means a **private key** must be used to **sign** the `JWT` and a **different public key** must be used to **verify** the signature.

- `JSON Web Key` (JWK) : A JSON object that represents a cryptographic key.
- `JSON Web Key Set` (JWKS) : A JSON object that represents a set of JWKs. the JSON object must have a keys member, which is an array of JWKs. Remember that `JWKS` containing the **public keys** used to verify any `JSON-Web-Token`.

**For the resourceserver :**

- opaquetoken.client-id
- opaquetoken.client-secret
- opaquetoken.introspection-uri

This configuration allows the token instropect. Why here ? The endpoints for this API type, `back-for-front`, are oriented to serve a front. Indeed, i implement this endpoint `introspect` for checking the `JWT` by the authorization server. I like this feature. But it is true that the `auth` endpoint provides a `JWT`, and inevitably that `JWT` is valid because it is provided by the authorization server. I will want check the `NimbusReactiveOpaqueTokenIntrospector`. My implementation is useless because normally the authorization server must returns the claims named `sub`. Unfortunately my authorization server doesn't return  a `JWT` with the `sub` claim in the payload of `JWT`.

## Endpoints

### info

`/info`

```json
{
	"path":"/info",
	"httpStatus":"401 UNAUTHORIZED"
}
```

This is the message when you need to connect. Otherwise you get a http-status equals to `OK 200`.

### Auth

`/auth`

This is the main endpoint to connect you. You get a maximum information like the `JWT` encoded and several `claims`.

```json
{
	"access_token":"eyJhbGciOiJSUKLoxMiIsImtpZCI6IlNhdENlcnQiLCJwaS5hdG0iOiIxIn0.eyJzY29wZSI6WyJvcGVuaWQiLCJlbWFpbCIsInByb2ZpbGUiLCJncm91cHMiLCJhZHZwcm9maWxlIl0sImNsaWVudF9pZCI6IjHbFTTA2YmE3OTc2ODMzYzliYWZjODllOGRiY2MzNzk5OTciLCJpc3MiOiJodHRwczovL6GjkcGIyZS1yZWMuYWRlby5jb20iLCJ1aWQiOiIyMDAxNDA3NyIsImV4cCI6MTYxNzU3MTM3MX0.fhKnryPcbX47Gs9VNlxOuwDlLdd35GMu6xBNuBz5R-cuo5Vyootna2qHtlZpMT0bwu6Nu710v6Kju67RIEjSzx2MCXZnyN5mez1fzyBBrpvMxTgB0lZ_6hEB3a0Q8fhicoeytZuvATDapEhXyqAqqKJOMWnBxAtmjYv7cX70h8H8hIK_PCw8Dj31Ajtbc-zFBt21YOuDUE_SEVH9P0d47l3ACiL4bM6EpWK2GHIlc1gJCoqmcexL-0PXfXXRN2R18KLKX9uWvxch4ynaLaY0sOfEUfGOZFQzJhxGwhYEj083doBxPFu2V0gXsf3tzEkiFu9PgULy1tbTWwc9GxQ",
	"claims":
		{
			"name":"Michael JORDAN",
			"ldap":"23",
			"email":"michael.jordan@bulls.com",
			"preferredlanguage":"en"
		}
}
```

### Claims

`/claims`

This is the source to gets the `claims` information.

```json
{
		"name":"Michael JORDAN",
		"ldap":"23",
		"email":"michael.jordan@bulls.com",
		"preferredlanguage":"en"
}
```

### Whoami

`/whoami`

Who am i ?

```json
{
	"ldap":"23"
}
```

### Introspect

`/introspect`

The introspect endpoint provides you the `JWT` verification.

In the best of case, you get this :

```json
{"value":true}
```
