package com.vattenfall.ecar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import java.time.LocalTime;

/**
 * Represents a transfer object for price creation/update.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceDto {

    private Integer id;

    @DecimalMin("0.0")
    private Double minuteRate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startHour;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endHour;
}
