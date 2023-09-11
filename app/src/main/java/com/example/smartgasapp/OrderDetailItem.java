package com.example.smartgasapp;

public class OrderDetailItem {

    String quantity;
    String type;
    String weight;
    Boolean exchange;

    public OrderDetailItem(String quantity, String type, String weight, Boolean exchange){
        this.quantity = quantity;
        this.type = type;
        this.weight = weight;
        this.exchange = false;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public Boolean getExchange() {
        return exchange;
    }

    public void setExchange(Boolean exchange) {
        this.exchange = exchange;
    }
}
