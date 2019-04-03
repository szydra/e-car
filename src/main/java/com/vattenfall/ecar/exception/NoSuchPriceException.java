package com.vattenfall.ecar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception that is thrown when there is no price with the given id.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoSuchPriceException extends RuntimeException {

    public NoSuchPriceException(Integer id) {
        super("Price with id " + id + " does not exist");
    }
}
