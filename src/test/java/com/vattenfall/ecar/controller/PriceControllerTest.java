package com.vattenfall.ecar.controller;

import com.vattenfall.ecar.dto.PriceDto;
import com.vattenfall.ecar.exception.NoSuchPriceException;
import com.vattenfall.ecar.exception.PriceException;
import com.vattenfall.ecar.service.Calculator;
import com.vattenfall.ecar.service.PriceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.Month;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PriceController.class)
public class PriceControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private Calculator calculator;

    @MockBean
    private PriceService priceService;

    @Test
    public void shouldRejectGetRequestWithInvalidDateRange() throws Exception {
        mvc.perform(get("/prices")
                .param("customer-id", "1")
                .param("start", "20190329T2040")
                .param("end", "20190328T0630"))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(calculator);
    }

    @Test
    public void shouldAcceptValidGetRequest() throws Exception {
        LocalDateTime start = LocalDateTime.of(2019, Month.MARCH, 29, 20, 40);
        LocalDateTime end = LocalDateTime.of(2019, Month.MARCH, 30, 6, 30);
        doReturn(12.34).when(calculator).calculate(1L, start, end);

        mvc.perform(get("/prices")
                .param("customer-id", "1")
                .param("start", "20190329T2040")
                .param("end", "20190330T0630"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value("12.34"));
    }

    @Test
    public void shouldRejectPostRequestWithId() throws Exception {
        mvc.perform(post("/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"minuteRate\":1.2}"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Use PATCH method for update"));
    }

    @Test
    public void shouldAcceptCorrectPostRequestAndSetLocationHeader() throws Exception {
        PriceDto priceDto = new PriceDto();
        priceDto.setId(1);
        doReturn(priceDto).when(priceService).doCreate(isA(PriceDto.class));

        mvc.perform(post("/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"minuteRate\":1.2,\"startHour\":\"04:30\",\"endHour\":\"06:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/prices/1"));
    }

    @Test
    public void shouldRejectPostRequestWithNegativeRate() throws Exception {
        mvc.perform(post("/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"minuteRate\":-2,\"startHour\":\"04:30\",\"endHour\":\"06:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectPostRequestWithStartTimeAndWithoutEndTime() throws Exception {
        mvc.perform(post("/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"minuteRate\":1.2,\"startHour\":\"04:30\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectPatchRequestWithNegativeRate() throws Exception {
        mvc.perform(patch("/prices/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"minuteRate\":-2}"))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(priceService);
    }

    @Test
    public void shouldAcceptPatchRequestWithValidRate() throws Exception {
        mvc.perform(patch("/prices/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"minuteRate\":2}"))
                .andExpect(status().isOk());

        verify(priceService).doUpdate(isA(PriceDto.class));
    }

    @Test
    public void shouldDeletePriceById() throws Exception {
        mvc.perform(delete("/prices/2"))
                .andExpect(status().isNoContent());

        verify(priceService).doDelete(2);
    }

    @Test
    public void shouldHandleNoSuchPriceException() throws Exception {
        doThrow(NoSuchPriceException.class).when(priceService).doUpdate(isA(PriceDto.class));

        mvc.perform(patch("/prices/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"minuteRate\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldHandlePriceException() throws Exception {
        doThrow(PriceException.class).when(priceService).doUpdate(isA(PriceDto.class));

        mvc.perform(patch("/prices/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"minuteRate\":1}"))
                .andExpect(status().isBadRequest());
    }

}
