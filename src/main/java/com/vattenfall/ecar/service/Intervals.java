package com.vattenfall.ecar.service;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * Utility class for calculating length in minutes of the intersection of time intervals.
 */
class Intervals {

    /**
     * Calculates the number of minutes which {@link DateTimeInterval} and {@link TimeInterval} have in common.
     *
     * @param dateTimeInterval date time interval
     * @param timeInterval     day time interval
     * @return length of the intersection in minutes, 0 if the intervals are disjoint
     */
    static long intersectionInMinutes(DateTimeInterval dateTimeInterval, TimeInterval timeInterval) {
        LocalDateTime startDateTime = dateTimeInterval.start;
        LocalDateTime endDateTime = dateTimeInterval.end;
        if (DAYS.between(startDateTime, endDateTime) > 0) {
            DateTimeInterval interval = new DateTimeInterval(startDateTime.plusDays(1), endDateTime);
            return timeInterval.length() + intersectionInMinutes(interval, timeInterval);
        } else {
            TimeInterval interval = new TimeInterval(startDateTime.toLocalTime(), endDateTime.toLocalTime());
            return intersectionInMinutes(interval, timeInterval);
        }
    }

    /**
     * Calculates the number of minutes which two {@link TimeInterval} objects have in common.
     * This function is symmetric.
     *
     * @param interval1 day time interval
     * @param interval2 day time interval
     * @return length of the intersection in minutes, 0 if the intervals are disjoint
     */
    static long intersectionInMinutes(TimeInterval interval1, TimeInterval interval2) {
        LocalTime start1 = interval1.start;
        LocalTime end1 = interval1.end;
        LocalTime start2 = interval2.start;
        LocalTime end2 = interval2.end;
        // In case where two periods contain midnight, intersection is disconnected
        if (start1.isAfter(end1) && start2.isAfter(end2)) {
            return MINUTES.between(LocalTime.of(0, 0), earlier(end1, end2))
                    + DAYS.getDuration().toMinutes() - MINUTES.between(LocalTime.of(0, 0), later(start1, start2));
        }
        // We can assume that interval1 doesn't contain midnight
        if (start1.isAfter(end1)) {
            return intersectionInMinutes(interval2, interval1);
        }
        if (start2.isBefore(end2)) {
            return Math.max(0, MINUTES.between(later(start1, start2), earlier(end1, end2)));
        } else {
            return Math.max(0, MINUTES.between(start1, earlier(end1, end2)))
                    + Math.max(0, MINUTES.between(later(start1, start2), end1));
        }
    }

    private static LocalTime earlier(LocalTime time1, LocalTime time2) {
        return time1.isBefore(time2) ? time1 : time2;
    }

    private static LocalTime later(LocalTime time1, LocalTime time2) {
        return time1.isBefore(time2) ? time2 : time1;
    }

    /**
     * Represents date time interval, i.e. interval between two {@link LocalDateTime} objects.
     */
    static class DateTimeInterval {
        private final LocalDateTime start;
        private final LocalDateTime end;

        DateTimeInterval(LocalDateTime start, LocalDateTime end) {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start has to be before end");
            }
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Represents day time interval, i.e. interval between two {@link LocalTime} objects.
     */
    static class TimeInterval {
        private final LocalTime start;
        private final LocalTime end;

        TimeInterval(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        private long length() {
            if (start.isBefore(end)) {
                return MINUTES.between(start, end);
            } else {
                return DAYS.getDuration().toMinutes() - MINUTES.between(end, start);
            }
        }
    }
}
