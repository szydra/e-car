package com.vattenfall.ecar.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = BasicOrSpecialValidator.class)
@interface BasicOrSpecial {

    String message() default "both start hour and end hour are required or none of them";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
