package com.vattenfall.ecar;

import com.vattenfall.ecar.model.Customer;
import com.vattenfall.ecar.repository.CustomerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ECarApplicationTest {

    private static final Logger logger = LoggerFactory.getLogger(ECarApplicationTest.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void contextLoads() {
        logger.info("Context loads!");
    }

    @Test
    public void shouldInvokeCommandLineRunner() {
        assertThat(customerRepository.findAll())
                .hasSize(3)
                .filteredOn(Customer::getVip)
                .hasSize(2);
    }

}
