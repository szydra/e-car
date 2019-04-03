package com.vattenfall.ecar.service;

import com.vattenfall.ecar.exception.NoSuchCustomerException;
import com.vattenfall.ecar.exception.NotEnoughDataException;
import com.vattenfall.ecar.model.Customer;
import com.vattenfall.ecar.model.Price;
import com.vattenfall.ecar.model.RateType;
import com.vattenfall.ecar.repository.CustomerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class CalculatorTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PriceService priceService;

    @InjectMocks
    private Calculator calculator;

    private LocalDateTime start = LocalDateTime.of(2019, Month.APRIL, 5, 10, 0);
    private LocalDateTime end = LocalDateTime.of(2019, Month.APRIL, 5, 20, 0);

    @Before
    public void init() {
        Customer customer = new Customer();
        customer.setVip(false);
        doReturn(Optional.of(customer)).when(customerRepository).findById(1L);
        calculator.setVipDiscount("10%");
    }

    @Test
    public void shouldThrowExceptionWhenCustomerIsMissing() {
        assertThatExceptionOfType(NoSuchCustomerException.class)
                .isThrownBy(() -> calculator.calculate(-1L, start, end))
                .withMessage("Customer with id -1 does not exist");
    }

    @Test
    public void shouldThrowExceptionWhenDataIsMissing() {
        assertThatExceptionOfType(NotEnoughDataException.class)
                .isThrownBy(() -> calculator.calculate(1L, start, end))
                .withMessage("Basic price is missing");
    }

    @Test
    public void shouldCalculateTheCostWhenBasicPriceIsGiven() {
        Price price = new Price();
        price.setMinuteRate(0.1);
        price.setRateType(RateType.BASIC);
        doReturn(Collections.singletonList(price)).when(priceService).findAll();

        assertThat(calculator.calculate(1L, start, end)).isEqualTo(60.0);
    }

    @Test
    public void shouldCalculateTheCostWhenBasicAndSpecialPricesAreGiven() {
        Price basic = new Price();
        basic.setMinuteRate(0.1);
        basic.setRateType(RateType.BASIC);
        Price special = new Price();
        special.setMinuteRate(0.08);
        special.setRateType(RateType.SPECIAL);
        special.setStartHour(LocalTime.of(15, 0));
        special.setEndHour(LocalTime.of(22, 0));

        doReturn(Arrays.asList(basic, special)).when(priceService).findAll();

        assertThat(calculator.calculate(1L, start, end)).isEqualTo(54.0);
    }

    @Test
    public void shouldCalculateCorrectlyForALongerPeriod() {
        Price basic = new Price();
        basic.setMinuteRate(0.2);
        basic.setRateType(RateType.BASIC);
        Price special = new Price();
        special.setMinuteRate(0.1);
        special.setRateType(RateType.SPECIAL);
        special.setStartHour(LocalTime.of(22, 0));
        special.setEndHour(LocalTime.of(2, 0));

        doReturn(Arrays.asList(basic, special)).when(priceService).findAll();

        assertThat(calculator.calculate(1L, start.minusDays(1), end)).isEqualTo(384.0);
    }

    @Test
    public void shouldCalculateWithDiscount() {
        Customer customer = new Customer();
        customer.setVip(true);
        doReturn(Optional.of(customer)).when(customerRepository).findById(1L);

        Price price = new Price();
        price.setMinuteRate(0.24);
        price.setRateType(RateType.BASIC);
        doReturn(Collections.singletonList(price)).when(priceService).findAll();

        assertThat(calculator.calculate(1L, start, end)).isEqualTo(129.6);
    }
}
