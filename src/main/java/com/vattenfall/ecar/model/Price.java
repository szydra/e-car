package com.vattenfall.ecar.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Price {

    @Id
    @GeneratedValue
    private Integer id;

    @NotNull
    @Min(0L)
    private Double minuteRate;

    private LocalTime startHour;

    private LocalTime endHour;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RateType rateType;

    @Override
    public String toString() {
        if (rateType == RateType.SPECIAL) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return String.format("Rate: %s, start hour: %s, end hour: %s",
                    minuteRate, formatter.format(startHour), formatter.format(endHour));
        } else {
            return "Basic price with rate: " + minuteRate;
        }
    }
}
