package com.vattenfall.ecar.init;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vattenfall.ecar.model.Customer;
import com.vattenfall.ecar.repository.CustomerRepository;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;

/**
 * Runs at server startup and loads customers from json file whose
 * location is specified in the property <code>customers</code>.
 * In case of specifying an invalid location, the server fails to start.
 * If no location is present, invocation of {@link #run(String...)}
 * is skipped and the server continues initialization.
 */
@Component
@ConditionalOnProperty("customers")
public class CustomersLoader implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(CustomersLoader.class);

    @Value("${customers}")
    private String fileLocation;

    private CustomerRepository customerRepository;

    /**
     * @param customerRepository autowired by Spring on bean creation
     */
    public CustomersLoader(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Loads sample customers to database.
     *
     * @param args main method arguments
     */
    @Override
    @SneakyThrows
    public void run(String... args) {
        File file = new FileSystemResourceLoader().getResource(fileLocation).getFile();
        Collection<Customer> customers = new ObjectMapper().readValue(file, new Customers());
        customerRepository.saveAll(customers);
        logger.info("{} customers loaded", customers.size());
    }

    private class Customers extends TypeReference<Collection<Customer>> {
    }
}
