package com.vattenfall.ecar.service;

import com.vattenfall.ecar.exception.PriceException;
import com.vattenfall.ecar.model.Price;
import com.vattenfall.ecar.service.Intervals.TimeInterval;

import java.util.Collection;
import java.util.Objects;

import static com.vattenfall.ecar.model.RateType.BASIC;

/**
 * Utility class which helps to keep prices integrity.
 */
class OverlappingValidator {

    /**
     * Validates passed price against the collection of existing prices.
     * Helpful in keeping integrity before creating/updating a price.
     *
     * @param price  price to be validated
     * @param prices existing prices
     * @throws PriceException if the passed price causes violations
     */
    void validate(Price price, Collection<Price> prices) {
        prices.removeIf(p -> Objects.equals(p.getId(), price.getId()));
        if (price.getRateType() == BASIC) {
            validateBasicPrice(prices);
        } else {
            validateSpecialPrice(price, prices);
        }
    }

    private void validateBasicPrice(Collection<Price> prices) {
        if (prices.stream().map(Price::getRateType).anyMatch(type -> type == BASIC)) {
            throw new PriceException("Two basic prices are not allowed.");
        }
    }

    private void validateSpecialPrice(Price specialPrice, Collection<Price> prices) {
        prices.removeIf(price -> price.getRateType() == BASIC);
        for (Price price : prices) {
            if (overlap(price, specialPrice)) {
                throw new PriceException("Price causing violations: " + price);
            }
        }
    }

    /**
     * Checks if periods of two prices overlap.
     *
     * @param price1 first price
     * @param price2 second price
     * @return true if periods of the passed prices overlap
     */
    boolean overlap(Price price1, Price price2) {
        TimeInterval interval1 = new TimeInterval(price1.getStartHour(), price1.getEndHour());
        TimeInterval interval2 = new TimeInterval(price2.getStartHour(), price2.getEndHour());
        return Intervals.intersectionInMinutes(interval1, interval2) != 0;
    }
}
