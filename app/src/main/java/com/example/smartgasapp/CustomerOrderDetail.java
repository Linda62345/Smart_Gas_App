package com.example.smartgasapp;

public class CustomerOrderDetail {
    String quantity;
    String type;
    String weight;
    String exchange;
    public CustomerOrderDetail(String quantity,String type,String weight){
        this.quantity = quantity;
        this.type = type;
        this.weight = weight;
        this.exchange = null;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getType() {
        return type;
    }

    public String getWeight() {
        return weight;
    }
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

}
