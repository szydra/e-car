package com.vattenfall.ecar.validation;

import org.hibernate.validator.constraints.ConstraintComposition;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
@Documented
@BasicOrSpecial
@MinuteRateNotNull
@ConstraintComposition
@Constraint(validatedBy = {})
public @interface NewPrice {

    String message() default "invalid new price";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
