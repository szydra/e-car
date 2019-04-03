package com.vattenfall.ecar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception that is thrown when there is no customer with the given id.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoSuchCustomerException extends RuntimeException {

    public NoSuchCustomerException(Long id) {
        super("Customer with id " + id + " does not exist");
    }
}
