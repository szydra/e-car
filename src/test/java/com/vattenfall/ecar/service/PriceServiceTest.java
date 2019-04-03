package com.vattenfall.ecar.service;

import com.vattenfall.ecar.dto.PriceDto;
import com.vattenfall.ecar.exception.NoSuchPriceException;
import com.vattenfall.ecar.exception.PriceException;
import com.vattenfall.ecar.model.Price;
import com.vattenfall.ecar.model.RateType;
import com.vattenfall.ecar.repository.PriceRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PriceServiceTest {

    @Mock
    private PriceRepository repository;

    @InjectMocks
    private PriceService service;

    @Before
    public void initRepository() {
        doAnswer(invocation -> invocation.getArgument(0)).when(repository).save(isA(Price.class));
    }

    @Test
    public void shouldCreateValidPrice() {
        PriceDto priceDto = new PriceDto();
        priceDto.setMinuteRate(1.0);
        priceDto.setStartHour(LocalTime.of(1, 0));
        priceDto.setEndHour(LocalTime.of(2, 0));

        PriceDto created = service.doCreate(priceDto);

        assertThat(created)
                .extracting(PriceDto::getMinuteRate, PriceDto::getStartHour, PriceDto::getEndHour)
                .containsExactly(1.0, LocalTime.of(1, 0), LocalTime.of(2, 0));
    }

    @Test
    public void shouldNotCreateInvalidPrice() {
        PriceDto priceDto = new PriceDto();
        priceDto.setMinuteRate(1.0);
        Price price = new Price();
        price.setId(1);
        price.setMinuteRate(2.0);
        price.setRateType(RateType.BASIC);
        doReturn(new ArrayList<>(Collections.singletonList(price))).when(repository).findAll();

        assertThatExceptionOfType(PriceException.class).isThrownBy(() -> service.doCreate(priceDto));
    }

    @Test
    public void shouldThrowExceptionWhenPriceCannotBeFound() {
        PriceDto priceDto = new PriceDto();
        priceDto.setId(1);
        doReturn(Optional.empty()).when(repository).findById(1);

        assertThatExceptionOfType(NoSuchPriceException.class).isThrownBy(() -> service.doUpdate(priceDto));
    }

    @Test
    public void shouldUpdateMinuteRate() {
        PriceDto priceDto = new PriceDto();
        priceDto.setId(1);
        priceDto.setMinuteRate(1.5);
        Price price = new Price();
        price.setId(1);
        price.setMinuteRate(2.0);
        price.setRateType(RateType.BASIC);
        doReturn(Optional.of(price)).when(repository).findById(1);

        PriceDto updated = service.doUpdate(priceDto);

        assertThat(updated.getMinuteRate()).isEqualTo(1.5);
    }

    @Test
    public void shouldNotUpdateStartHourInBasicPrice() {
        PriceDto priceDto = new PriceDto();
        priceDto.setId(1);
        priceDto.setStartHour(LocalTime.of(2, 0));
        Price price = new Price();
        price.setId(1);
        price.setMinuteRate(2.0);
        price.setRateType(RateType.BASIC);
        doReturn(Optional.of(price)).when(repository).findById(1);

        PriceDto updated = service.doUpdate(priceDto);

        assertThat(updated.getStartHour()).isNull();
    }

    @Test
    public void shouldNotUpdateEndHourInBasicPrice() {
        PriceDto priceDto = new PriceDto();
        priceDto.setId(1);
        priceDto.setEndHour(LocalTime.of(2, 0));
        Price price = new Price();
        price.setId(1);
        price.setMinuteRate(2.0);
        price.setRateType(RateType.BASIC);
        doReturn(Optional.of(price)).when(repository).findById(1);

        PriceDto updated = service.doUpdate(priceDto);

        assertThat(updated.getEndHour()).isNull();
    }

    @Test
    public void shouldUpdateStartAndEndHourInSpecialPrice() {
        PriceDto priceDto = new PriceDto();
        priceDto.setId(1);
        priceDto.setMinuteRate(2.0);
        priceDto.setStartHour(LocalTime.of(1, 0));
        priceDto.setEndHour(LocalTime.of(2, 0));
        Price price = new Price();
        price.setId(1);
        price.setRateType(RateType.SPECIAL);
        doReturn(Optional.of(price)).when(repository).findById(1);

        PriceDto updated = service.doUpdate(priceDto);

        assertThat(updated).isEqualToComparingFieldByField(priceDto);
    }

    @Test
    public void shouldDeleteById() {
        service.doDelete(1);

        verify(repository).deleteById(1);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void shouldFindAll() {
        service.findAll();

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }
}
