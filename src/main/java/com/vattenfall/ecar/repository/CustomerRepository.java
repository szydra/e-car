package com.vattenfall.ecar.repository;

import com.vattenfall.ecar.model.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
}
