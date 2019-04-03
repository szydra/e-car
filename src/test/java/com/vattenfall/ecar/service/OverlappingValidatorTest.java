package com.vattenfall.ecar.service;

import com.vattenfall.ecar.exception.PriceException;
import com.vattenfall.ecar.model.Price;
import com.vattenfall.ecar.model.RateType;
import org.junit.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;

public class OverlappingValidatorTest {

    private OverlappingValidator validator = new OverlappingValidator();

    /*
     * Tests for validate method
     */
    private Price createPrice(Integer id, LocalTime start, LocalTime end, RateType type) {
        Price price = createPrice(start, end);
        price.setId(id);
        price.setRateType(type);
        return price;
    }

    @Test
    public void shouldBePossibleToAddBasicPriceToEmptyList() {
        Price price = createPrice(1, null, null, RateType.BASIC);

        assertThatCode(() -> validator.validate(price, Collections.emptyList()))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldBePossibleToAddSpecialPriceToEmptyList() {
        Price price = createPrice(1, null, null, RateType.SPECIAL);

        assertThatCode(() -> validator.validate(price, Collections.emptyList()))
                .doesNotThrowAnyException();
    }

    @Test
    public void secondBasicPriceShouldBeForbidden() {
        Price oldPrice = createPrice(1, null, null, RateType.BASIC);
        Price newPrice = createPrice(null, null, null, RateType.BASIC);

        List<Price> existingPrices = new ArrayList<>(singletonList(oldPrice));

        assertThatExceptionOfType(PriceException.class)
                .isThrownBy(() -> validator.validate(newPrice, existingPrices))
                .withMessage("Two basic prices are not allowed.");
    }

    @Test
    public void shouldBeAllowedToAddSpecialPrice() {
        Price basicPrice = createPrice(1, null, null, RateType.BASIC);
        Price newPrice = createPrice(null, LocalTime.of(1, 0), LocalTime.of(2, 0), RateType.SPECIAL);

        List<Price> existingPrices = new ArrayList<>(singletonList(basicPrice));

        assertThatCode(() -> validator.validate(newPrice, existingPrices))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldBeAllowedToAddSpecialPriceThatDoesNotCauseViolations() {
        Price oldPrice = createPrice(1, LocalTime.of(1, 0), LocalTime.of(2, 0), RateType.SPECIAL);
        Price newPrice = createPrice(null, LocalTime.of(2, 0), LocalTime.of(3, 0), RateType.SPECIAL);

        List<Price> existingPrices = new ArrayList<>(singletonList(oldPrice));

        assertThatCode(() -> validator.validate(newPrice, existingPrices))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldBeForbiddenToAddSpecialPriceThatDoesCauseViolations() {
        Price oldPrice = createPrice(1, LocalTime.of(1, 0), LocalTime.of(2, 0), RateType.SPECIAL);
        Price newPrice = createPrice(null, LocalTime.of(1, 0), LocalTime.of(3, 0), RateType.SPECIAL);

        List<Price> existingPrices = new ArrayList<>(singletonList(oldPrice));

        assertThatExceptionOfType(PriceException.class)
                .isThrownBy(() -> validator.validate(newPrice, existingPrices))
                .withMessageStartingWith("Price causing violations");
    }

    @Test
    public void shouldBePossibleToUpdateBasicPriceValue() {
        Price basicPrice = createPrice(1, null, null, RateType.BASIC);

        List<Price> existingPrices = new ArrayList<>(singletonList(basicPrice));

        assertThatCode(() -> validator.validate(basicPrice, existingPrices))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldBeForbiddenToUpdateSpecialPriceThatDoesCauseViolations() {
        Price oldPrice = createPrice(1, LocalTime.of(1, 0), LocalTime.of(2, 0), RateType.SPECIAL);
        Price newPrice = createPrice(2, LocalTime.of(1, 0), LocalTime.of(3, 0), RateType.SPECIAL);

        List<Price> existingPrices = new ArrayList<>(singletonList(oldPrice));

        assertThatExceptionOfType(PriceException.class)
                .isThrownBy(() -> validator.validate(newPrice, existingPrices))
                .withMessageStartingWith("Price causing violations");
    }

    @Test
    public void shouldBeAllowedToUpdateSpecialPriceThatDoesNotCauseViolations() {
        Price oldPrice = createPrice(1, LocalTime.of(1, 0), LocalTime.of(2, 0), RateType.SPECIAL);
        Price newPrice = createPrice(2, LocalTime.of(2, 0), LocalTime.of(3, 0), RateType.SPECIAL);

        List<Price> existingPrices = new ArrayList<>(singletonList(oldPrice));

        assertThatCode(() -> validator.validate(newPrice, existingPrices))
                .doesNotThrowAnyException();
    }

    /*
     * Tests for overlap method
     */
    private Price createPrice(LocalTime start, LocalTime end) {
        Price price = new Price();
        price.setStartHour(start);
        price.setEndHour(end);
        return price;
    }

    @Test
    public void shouldNotOverlapWhenPeriodsAreDisjoint() {
        Price price1 = createPrice(LocalTime.of(2, 30), LocalTime.of(3, 45));
        Price price2 = createPrice(LocalTime.of(12, 30), LocalTime.of(13, 45));

        assertThat(validator.overlap(price1, price2)).isFalse();
    }

    @Test
    public void shouldNotOverlapWhenPeriodsIntersectInOnePoint() {
        Price price1 = createPrice(LocalTime.of(2, 30), LocalTime.of(3, 45));
        Price price2 = createPrice(LocalTime.of(3, 45), LocalTime.of(13, 45));

        assertThat(validator.overlap(price1, price2)).isFalse();
    }

    @Test
    public void shouldOverlapWhenPeriodsIntersectNonTrivially() {
        Price price1 = createPrice(LocalTime.of(2, 30), LocalTime.of(3, 45));
        Price price2 = createPrice(LocalTime.of(3, 30), LocalTime.of(13, 45));

        assertThat(validator.overlap(price1, price2)).isTrue();
    }

    @Test
    public void shouldOverlapWhenPeriodsIntersectNonTriviallyAndEndIsBeforeStart1() {
        Price price1 = createPrice(LocalTime.of(12, 30), LocalTime.of(3, 45));
        Price price2 = createPrice(LocalTime.of(3, 30), LocalTime.of(10, 45));

        assertThat(validator.overlap(price1, price2)).isTrue();
    }

    @Test
    public void shouldOverlapWhenPeriodsIntersectNonTriviallyAndEndIsBeforeStart2() {
        Price price1 = createPrice(LocalTime.of(2, 30), LocalTime.of(3, 45));
        Price price2 = createPrice(LocalTime.of(3, 30), LocalTime.of(3, 15));

        assertThat(validator.overlap(price1, price2)).isTrue();
    }

    @Test
    public void midnightShouldNotCauseProblems1() {
        Price price1 = createPrice(LocalTime.of(0, 0), LocalTime.of(3, 45));
        Price price2 = createPrice(LocalTime.of(4, 30), LocalTime.of(0, 0));

        assertThat(validator.overlap(price1, price2)).isFalse();
    }

    @Test
    public void midnightShouldNotCauseProblems2() {
        Price price1 = createPrice(LocalTime.of(0, 0), LocalTime.of(3, 45));
        Price price2 = createPrice(LocalTime.of(3, 30), LocalTime.of(0, 0));

        assertThat(validator.overlap(price1, price2)).isTrue();
    }

    @Test
    public void twoPeriodsWithMidnightShouldOverlap() {
        Price price1 = createPrice(LocalTime.of(23, 0), LocalTime.of(3, 0));
        Price price2 = createPrice(LocalTime.of(21, 30), LocalTime.of(1, 0));

        assertThat(validator.overlap(price1, price2)).isTrue();
    }

    @Test
    public void periodAndItsCompletionShouldNotOverlap() {
        Price price1 = createPrice(LocalTime.of(23, 0), LocalTime.of(3, 0));
        Price price2 = createPrice(LocalTime.of(3, 0), LocalTime.of(23, 0));

        assertThat(validator.overlap(price1, price2)).isFalse();
    }
}
