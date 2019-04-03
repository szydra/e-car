package com.vattenfall.ecar.controller;

import com.vattenfall.ecar.dto.PriceDto;
import com.vattenfall.ecar.exception.NoSuchPriceException;
import com.vattenfall.ecar.exception.PriceException;
import com.vattenfall.ecar.service.Calculator;
import com.vattenfall.ecar.service.PriceService;
import com.vattenfall.ecar.validation.NewPrice;
import lombok.SneakyThrows;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic controller for price management.
 * GET, POST, DELETE and PATCH methods are supported.
 */
@RestController
@RequestMapping("/prices")
@Validated
public class PriceController {

    private Calculator calculator;
    private PriceService priceService;

    public PriceController(Calculator calculator, PriceService priceService) {
        this.calculator = calculator;
        this.priceService = priceService;
    }

    /**
     * Handles GET method with request parameters <code>customer-id</code>,
     * <code>start</code> and <code>end</code>.
     *
     * @return total price
     */
    @GetMapping(params = {"customer-id", "start", "end"})
    public Map<String, Double> calculatePrice(@RequestParam("customer-id") Long customerId,
                                              @DateTimeFormat(pattern = "yyyyMMdd'T'HHmm")
                                              @RequestParam("start") LocalDateTime start,
                                              @DateTimeFormat(pattern = "yyyyMMdd'T'HHmm")
                                              @RequestParam("end") LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date has to be before end date");
        }
        return Collections.singletonMap("total", calculator.calculate(customerId, start, end));
    }

    /**
     * Handles POST requests and creates new prices.
     *
     * @param newPrice valid body of the request
     * @return created price
     * @throws PriceException if the price cannot be created
     */
    @PostMapping
    @SneakyThrows
    public ResponseEntity<PriceDto> addNewPrice(@Valid @NewPrice @RequestBody PriceDto newPrice) {
        if (newPrice.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use PATCH method for update");
        }
        PriceDto createdPrice = priceService.doCreate(newPrice);
        return ResponseEntity
                .created(new URI("/prices/" + createdPrice.getId()))
                .body(createdPrice);
    }

    /**
     * Handles PATCH requests and updates prices.
     *
     * @param id       id of an existing price
     * @param priceDto valid body of the request
     * @return updated price
     * @throws NoSuchPriceException if the price cannot be found
     * @throws PriceException       if the price cannot be updated
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PriceDto> updatePrice(@PathVariable Integer id, @Valid @RequestBody PriceDto priceDto) {
        priceDto.setId(id);
        return ResponseEntity.ok(priceService.doUpdate(priceDto));
    }

    /**
     * Handles DELETE requests and deletes prices.
     *
     * @param id price id
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        priceService.doDelete(id);
    }

    /**
     * Handles {@link ConstraintViolationException} and maps it to http status 400 instead of default 500.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException exception) {
        String violations = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(Collections.singletonMap("message", violations));
    }
}
