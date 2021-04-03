package com.example.oauth2springreactiveexemple.controller.exception;

public class UnauthorizeException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public UnauthorizeException() {}
	
	public UnauthorizeException(String message) {
		super(message);
	}
	
	public UnauthorizeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnauthorizeException(Throwable cause) {
		super(cause);
	}
}
