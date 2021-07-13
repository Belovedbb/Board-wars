package com.board.wars.payload.response;

import java.util.List;

public class ActivityFrequencyDataResponsePayload {
    private String title;
    private List<ActivityFrequencyDataPointResponsePayload> months;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ActivityFrequencyDataPointResponsePayload> getMonths() {
        return months;
    }

    public void setMonths(List<ActivityFrequencyDataPointResponsePayload> months) {
        this.months = months;
    }
}
