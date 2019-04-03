package com.vattenfall.ecar.service;

import com.vattenfall.ecar.dto.Mapper;
import com.vattenfall.ecar.dto.PriceDto;
import com.vattenfall.ecar.exception.NoSuchPriceException;
import com.vattenfall.ecar.exception.PriceException;
import com.vattenfall.ecar.model.Price;
import com.vattenfall.ecar.model.RateType;
import com.vattenfall.ecar.repository.PriceRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * <p>Service for price management. It allows to define, update and delete prices.
 * It is also used for reading all available prices.</p>
 *
 * <p>Methods in this class are not thread-safe so it is recommended to add some additional
 * database constraint to ensure data integrity. Sample definition of such a constraint
 * for H2 can be found in the validate_time.sql file.</p>
 */
@Service
public class PriceService {

    private Mapper mapper = new Mapper();
    private OverlappingValidator validator = new OverlappingValidator();
    private PriceRepository repository;

    public PriceService(PriceRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a price and refreshes the application cache.
     *
     * @param priceDto transfer object received in request
     * @return created price
     * @throws PriceException if the created price causes data integrity
     */
    @Transactional
    @CacheEvict(value = "prices", allEntries = true)
    public PriceDto doCreate(PriceDto priceDto) {
        Price price = mapper.mapToModel(priceDto);
        validator.validate(price, repository.findAll());
        return mapper.mapToDto(repository.save(price));
    }

    /**
     * Updates a price and refreshes the application cache.
     *
     * @param priceDto transfer object received in request
     * @return updated price
     * @throws NoSuchPriceException if the price cannot be found
     * @throws PriceException       if the updated price causes data integrity
     */
    @Transactional
    @CacheEvict(value = "prices", allEntries = true)
    public PriceDto doUpdate(PriceDto priceDto) {
        Price price = repository.findById(priceDto.getId())
                .orElseThrow(() -> new NoSuchPriceException(priceDto.getId()));
        updateProperties(price, priceDto);
        validator.validate(price, repository.findAll());
        return mapper.mapToDto(price);
    }

    private void updateProperties(Price price, PriceDto priceDto) {
        if (priceDto.getMinuteRate() != null) {
            price.setMinuteRate(priceDto.getMinuteRate());
        }
        if (price.getRateType() == RateType.SPECIAL) {
            updateTimeInterval(price, priceDto);
        }
    }

    private void updateTimeInterval(Price price, PriceDto priceDto) {
        if (priceDto.getStartHour() != null) {
            price.setStartHour(priceDto.getStartHour());
        }
        if (priceDto.getEndHour() != null) {
            price.setEndHour(priceDto.getEndHour());
        }
    }

    /**
     * Deletes a price if exists and refreshes the application cache.
     *
     * @param id price id
     */
    @Transactional
    @CacheEvict(value = "prices", allEntries = true)
    public void doDelete(Integer id) {
        repository.deleteById(id);
    }

    /**
     * Returns all prices either from database or from the application cache.
     * By default it uses the built in mechanism, but some external cache provider
     * can be add to the classpath and registered.
     *
     * @return all prices
     */
    @Cacheable("prices")
    public Collection<Price> findAll() {
        return repository.findAll();
    }
}
