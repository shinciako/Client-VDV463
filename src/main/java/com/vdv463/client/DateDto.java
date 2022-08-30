package com.vdv463.client;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class DateDto {
    public List<DateConverter> dateConverters = new ArrayList<>();
    public void addDateConverter(DateConverter dateConverter){
        this.dateConverters.add(dateConverter);
    }
}
