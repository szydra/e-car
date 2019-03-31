package com.vattenfall.ecar.validation;

import com.vattenfall.ecar.dto.PriceDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalTime;

class BasicOrSpecialValidator implements ConstraintValidator<BasicOrSpecial, PriceDto> {

    @Override
    public boolean isValid(PriceDto price, ConstraintValidatorContext context) {
        LocalTime startHour = price.getStartHour();
        LocalTime endHour = price.getEndHour();
        return (startHour == null && endHour == null) || (startHour != null && endHour != null);
    }

}
