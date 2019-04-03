package com.vattenfall.ecar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception that is thrown when a price cannot be created/updated.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PriceException extends RuntimeException {

    public PriceException(String message) {
        super(message);
    }
}
