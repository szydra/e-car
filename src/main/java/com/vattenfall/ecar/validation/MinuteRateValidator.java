package com.vattenfall.ecar.validation;

import com.vattenfall.ecar.dto.PriceDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class MinuteRateValidator implements ConstraintValidator<MinuteRateNotNull, PriceDto> {

    @Override
    public boolean isValid(PriceDto price, ConstraintValidatorContext context) {
        return price.getMinuteRate() != null;
    }

}
