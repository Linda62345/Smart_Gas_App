package com.example.smartgasapp;

public class OrderListFinishList {
    String time, condition;

    public OrderListFinishList(String time, String condition) {
        this.time = time;
        this.condition = condition;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
