package com.vdv463.client;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class DateConverter {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date date;
    int hour;
    int minute;
    int second;
    int nanoOfSecond;

    List<String> time = new ArrayList<>(List.of("ExpectedArrivalTime","RequestedTimeForDeparture", "RequestedFinishTime"));
    public ZonedDateTime convertToZonedDateTime() {
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return ZonedDateTime.of(
                localDateTime.getYear(),
                localDateTime.getMonthValue(),
                localDateTime.getDayOfMonth(),
                hour,
                minute,
                second,
                nanoOfSecond,
                ZoneId.of("Z"));
    }
}
