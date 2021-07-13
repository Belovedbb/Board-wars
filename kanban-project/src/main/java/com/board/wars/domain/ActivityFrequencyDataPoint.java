package com.board.wars.domain;

public class ActivityFrequencyDataPoint {
    private String month;
    private Long projectCount;
    private Long taskCount;

    public ActivityFrequencyDataPoint(String month, Long projectCount, Long taskCount) {
        this.month = month;
        this.projectCount = projectCount;
        this.taskCount = taskCount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Long getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(Long projectCount) {
        this.projectCount = projectCount;
    }

    public Long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Long taskCount) {
        this.taskCount = taskCount;
    }
}
