package com.bitpops.barcode.Model;

import java.util.HashMap;
import java.util.Map;

public class Product {

    Map<String,String> map;
    public Product()
    {
        map = new HashMap<String,String>();
    }

    public Map<String, String> getProductProperties() {
        return map;
    }

    public void setProductProperties(Map<String, String> map) {
        this.map = map;
    }
    public void addProductProperty(String key, String value) {
        this.map.put(key, value);
    }
    public void removeProductProperty(String key) {
        this.map.remove(key);
    }
}
