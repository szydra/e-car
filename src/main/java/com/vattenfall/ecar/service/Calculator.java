package com.vattenfall.ecar.service;

import com.vattenfall.ecar.exception.NoSuchCustomerException;
import com.vattenfall.ecar.exception.NotEnoughDataException;
import com.vattenfall.ecar.model.Customer;
import com.vattenfall.ecar.model.Price;
import com.vattenfall.ecar.model.RateType;
import com.vattenfall.ecar.repository.CustomerRepository;
import com.vattenfall.ecar.service.Intervals.DateTimeInterval;
import com.vattenfall.ecar.service.Intervals.TimeInterval;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.vattenfall.ecar.service.Intervals.intersectionInMinutes;

/**
 * Service which is responsible for calculating prices of customer's charging process.
 */
@Service
public class Calculator {

    private PriceService priceService;
    private CustomerRepository customerRepository;
    private double vipDiscount;

    public Calculator(PriceService priceService, CustomerRepository customerRepository) {
        this.priceService = priceService;
        this.customerRepository = customerRepository;
    }

    @Autowired
    @SneakyThrows
    public void setVipDiscount(@Value("${vip.discount}") String vipDiscount) {
        this.vipDiscount = new DecimalFormat("0.0#%").parse(vipDiscount).doubleValue();
    }

    /**
     * Calculates the total cost of customer's charging process.
     * Lowers the price for vip customers.
     *
     * @param customerId customer id
     * @param start      start time of charging process
     * @param end        end time of charging process
     * @return total cost rounded to two decimal places
     * @throws NoSuchCustomerException if there is no customer with the passed id
     * @throws NotEnoughDataException  if there are not enough prices defined to calculate the total cost
     */
    public Double calculate(Long customerId, LocalDateTime start, LocalDateTime end) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchCustomerException(customerId));
        DateTimeInterval chargingInterval = new DateTimeInterval(start, end);
        long chargingTime = ChronoUnit.MINUTES.between(start, end);
        long totalTime = getTimeInSpecialPeriods(chargingInterval);
        double totalPrice = getPriceInSpecialPeriods(chargingInterval);
        if (totalTime < chargingTime) {
            totalPrice += (chargingTime - totalTime) * getBasicRate();
        }
        double discountedPrice = customer.getVip() ? (1 - vipDiscount) * totalPrice : totalPrice;
        return round(discountedPrice);
    }

    private long getTimeInSpecialPeriods(DateTimeInterval interval) {
        return priceService.findAll().stream()
                .filter(price -> price.getRateType() == RateType.SPECIAL)
                .map(price -> new TimeInterval(price.getStartHour(), price.getEndHour()))
                .mapToLong(time -> intersectionInMinutes(interval, time))
                .sum();
    }

    private double getPriceInSpecialPeriods(DateTimeInterval interval) {
        return priceService.findAll().stream()
                .filter(price -> price.getRateType() == RateType.SPECIAL)
                .mapToDouble(price -> {
                    TimeInterval timeInterval = new TimeInterval(price.getStartHour(), price.getEndHour());
                    return intersectionInMinutes(interval, timeInterval) * price.getMinuteRate();
                }).sum();
    }

    private double getBasicRate() {
        return priceService.findAll().stream()
                .filter(price -> price.getRateType() == RateType.BASIC)
                .map(Price::getMinuteRate)
                .findAny()
                .orElseThrow(() -> new NotEnoughDataException("Basic price is missing"));
    }

    private double round(double discountedPrice) {
        return new BigDecimal(discountedPrice).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
