package com.example.seestore.controller;

import android.content.Context;


import com.example.seestore.model.OrderItem;
import com.example.seestore.model.Product;

import java.util.List;

public class CartController {
    private Context context;
    private List<OrderItem> orderItemList;

    public CartController(Context context) {
        this.context = context;

    }

    public List<Product> getCartList() {
        return null;
    }

    public double getSubTotal() {
        double result = 0f;

        for (OrderItem item : orderItemList) {
//            result += item.getCost();
        }


        return result;
    }

    public int getOrderListSize() {
        return orderItemList.size();
    }
}