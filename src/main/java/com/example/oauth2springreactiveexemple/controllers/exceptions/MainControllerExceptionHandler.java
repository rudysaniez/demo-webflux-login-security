package com.example.oauth2springreactiveexemple.controllers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class MainControllerExceptionHandler {

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(value = UnauthorizeException.class)
	public @ResponseBody HttpErrorInfo notFoundException(ServerHttpRequest request, Exception exception) {
		return createHttpErrorInfo(HttpStatus.UNAUTHORIZED, request, exception);
	}
	
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(value = OAuth2IntrospectionException.class)
	public @ResponseBody HttpErrorInfo invalidInputException(ServerHttpRequest request, Exception exception) {
		return createHttpErrorInfo(HttpStatus.UNAUTHORIZED, request, exception);
	}
	
	/**
	 * @param httpStatus
	 * @param request
	 * @param ex
	 * @return {@link HttpErrorInfo}
	 */
	private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception ex) {
		
        final String path = request.getPath().pathWithinApplication().value();
        final String message = ex.getMessage();

    	log.debug(" > Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);
        
    	return new HttpErrorInfo().message(message).path(path).
    			httpStatus(httpStatus.toString());
    }
}
