package com.board.wars.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class ActivityFrequencyData extends BaseEntity {
    @Indexed
    private String title;
    private List<ActivityFrequencyDataPoint> months;

    public ActivityFrequencyData(String title, List<ActivityFrequencyDataPoint> months) {
        this.title = title;
        this.months = months;
    }

    public ActivityFrequencyData() {

    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ActivityFrequencyDataPoint> getMonths() {
        return months;
    }

    public void setMonths(List<ActivityFrequencyDataPoint> months) {
        this.months = months;
    }
}
