package com.vattenfall.ecar.service;

import com.vattenfall.ecar.service.Intervals.DateTimeInterval;
import com.vattenfall.ecar.service.Intervals.TimeInterval;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


public class IntervalsTest {

    @Test
    public void shouldCalculateIntersectionOfNestedIntervals() {
        LocalDate date = LocalDate.of(2019, Month.APRIL, 1);
        LocalTime start = LocalTime.of(10, 30);
        LocalTime end = LocalTime.of(20, 30);
        DateTimeInterval dateTimeInterval = new DateTimeInterval(LocalDateTime.of(date, start), LocalDateTime.of(date, end));

        TimeInterval timeInterval = new TimeInterval(LocalTime.of(12, 15), LocalTime.of(16, 0));

        assertThat(Intervals.intersectionInMinutes(dateTimeInterval, timeInterval)).isEqualTo(45 + 3 * 60);
    }

    @Test
    public void shouldCalculateIntersectionOfNonNestedIntervals() {
        LocalDate date = LocalDate.of(2019, Month.APRIL, 1);
        LocalTime start = LocalTime.of(10, 30);
        LocalTime end = LocalTime.of(20, 30);
        DateTimeInterval dateTimeInterval = new DateTimeInterval(LocalDateTime.of(date, start), LocalDateTime.of(date, end));

        TimeInterval timeInterval = new TimeInterval(LocalTime.of(12, 15), LocalTime.of(23, 0));

        assertThat(Intervals.intersectionInMinutes(dateTimeInterval, timeInterval)).isEqualTo(45 + 7 * 60 + 30);
    }

    @Test
    public void shouldCalculateIntersectionOfLongerThanOneDayIntervalWithIntervalNonContainingMidnight() {
        LocalDate startDate = LocalDate.of(2019, Month.APRIL, 1);
        LocalDate endDate = LocalDate.of(2019, Month.APRIL, 5);
        LocalTime startTime = LocalTime.of(10, 30);
        LocalTime endTime = LocalTime.of(20, 30);
        DateTimeInterval dateTimeInterval = new DateTimeInterval(LocalDateTime.of(startDate, startTime), LocalDateTime.of(endDate, endTime));

        TimeInterval timeInterval = new TimeInterval(LocalTime.of(12, 15), LocalTime.of(13, 0));

        assertThat(Intervals.intersectionInMinutes(dateTimeInterval, timeInterval)).isEqualTo(5 * 45);
    }

    @Test
    public void shouldCalculateIntersectionOfLongerThanOneDayWithIntervalWithIntervalContainingMidnight() {
        LocalDate startDate = LocalDate.of(2019, Month.APRIL, 1);
        LocalDate endDate = LocalDate.of(2019, Month.APRIL, 5);
        LocalTime startTime = LocalTime.of(10, 30);
        LocalTime endTime = LocalTime.of(20, 30);
        DateTimeInterval dateTimeInterval = new DateTimeInterval(LocalDateTime.of(startDate, startTime), LocalDateTime.of(endDate, endTime));

        TimeInterval timeInterval = new TimeInterval(LocalTime.of(22, 0), LocalTime.of(1, 20));

        assertThat(Intervals.intersectionInMinutes(dateTimeInterval, timeInterval)).isEqualTo(4 * 200);
    }

    @Test
    public void shouldCalculateIntersectionOfTwoIntervalsContainingMidnight() {
        TimeInterval interval1 = new TimeInterval(LocalTime.of(22, 0), LocalTime.of(3, 15));
        TimeInterval interval2 = new TimeInterval(LocalTime.of(20, 30), LocalTime.of(1, 15));

        assertThat(Intervals.intersectionInMinutes(interval1, interval2)).isEqualTo(3 * 60 + 15);
    }

    @Test
    public void shouldCalculateIntersectionOfTwoIntervalsContainingMidnight2() {
        TimeInterval interval1 = new TimeInterval(LocalTime.of(21, 30), LocalTime.of(8, 0));
        TimeInterval interval2 = new TimeInterval(LocalTime.of(22, 0), LocalTime.of(5, 0));

        assertThat(Intervals.intersectionInMinutes(interval1, interval2)).isEqualTo(7 * 60);
    }

    @Test
    public void shouldCalculateIntersectionWhenFirstIntervalContainsMidnight() {
        TimeInterval interval1 = new TimeInterval(LocalTime.of(22, 0), LocalTime.of(3, 15));
        TimeInterval interval2 = new TimeInterval(LocalTime.of(0, 30), LocalTime.of(1, 15));

        assertThat(Intervals.intersectionInMinutes(interval1, interval2)).isEqualTo(45);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenCreatingInvalidDateTimeInterval() {
        LocalDate date = LocalDate.of(2019, Month.APRIL, 1);
        LocalTime startTime = LocalTime.of(13, 35);
        LocalTime endTime = LocalTime.of(13, 34);
        LocalDateTime start = LocalDateTime.of(date, startTime);
        LocalDateTime end = LocalDateTime.of(date, endTime);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DateTimeInterval(start, end))
                .withMessage("Start has to be before end");
    }
}
