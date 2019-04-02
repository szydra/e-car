package com.vattenfall.ecar.repository;

import com.vattenfall.ecar.model.Price;
import org.springframework.data.repository.CrudRepository;

public interface PriceRepository extends CrudRepository<Price, Integer> {
}
