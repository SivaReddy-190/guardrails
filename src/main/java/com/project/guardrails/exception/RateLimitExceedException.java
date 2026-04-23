package com.project.guardrails.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceedException extends RuntimeException{

    public RateLimitExceedException(String message) {
        super(message);
    }

}
