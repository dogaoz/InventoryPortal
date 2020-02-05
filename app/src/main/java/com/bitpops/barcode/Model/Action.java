package com.bitpops.barcode.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Action {

    String businessName = "";
    String locationFrom = "";
    String locationTo = "";
    String actionType = ""; // Transfer, Delivery, Something else
    String productStatuses = ""; // Received etc.
    int deviceId = -99;
    ArrayList<Product> products;

    public Action()
    {
        products = new ArrayList<>();
    }
    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
    public String getLocationFrom() {
        return locationFrom;
    }

    public void setLocationFrom(String locationFrom) {
        this.locationFrom = locationFrom;
    }
    public String getLocationTo() {
        return locationTo;
    }

    public void setLocationTo(String locationTo) {
        this.locationTo = locationTo;
    }
    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    public String getProductStatuses() {
        return productStatuses;
    }

    public void setProductStatuses(String productStatuses) {
        this.productStatuses = productStatuses;
    }
}
