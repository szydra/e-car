package com.vattenfall.ecar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception that is thrown when a price definition is missing.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class NotEnoughDataException extends RuntimeException {

    public NotEnoughDataException(String message) {
        super(message);
    }
}
