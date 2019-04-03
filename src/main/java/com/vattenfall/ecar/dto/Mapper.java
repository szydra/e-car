package com.vattenfall.ecar.dto;

import com.vattenfall.ecar.model.Price;
import com.vattenfall.ecar.model.RateType;

/**
 * Maps between {@link PriceDto} and {@link Price}.
 */
public class Mapper {

    public PriceDto mapToDto(Price price) {
        PriceDto priceDto = new PriceDto();
        priceDto.setId(price.getId());
        priceDto.setMinuteRate(price.getMinuteRate());
        if (price.getRateType() == RateType.SPECIAL) {
            priceDto.setStartHour(price.getStartHour());
            priceDto.setEndHour(price.getEndHour());
        }
        return priceDto;
    }

    public Price mapToModel(PriceDto priceDto) {
        Price price = new Price();
        price.setId(priceDto.getId());
        price.setMinuteRate(priceDto.getMinuteRate());
        price.setStartHour(priceDto.getStartHour());
        price.setEndHour(priceDto.getEndHour());
        price.setRateType(determineRateType(priceDto));
        return price;
    }

    private RateType determineRateType(PriceDto priceDto) {
        if (priceDto.getStartHour() == null && priceDto.getEndHour() == null) {
            return RateType.BASIC;
        } else {
            return RateType.SPECIAL;
        }
    }
}
