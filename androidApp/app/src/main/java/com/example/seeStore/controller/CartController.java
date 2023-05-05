package com.example.seeStore.controller;

import android.content.Context;


import com.example.seeStore.model.OrderItem;
import com.example.seeStore.model.Product;

import java.util.List;
import java.util.ArrayList;
public class CartController {
    private Context context;
    private List<OrderItem> orderItemList;

    public CartController(Context context) {
        this.context = context;

        Long i = new Long(0);
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(150);
        orderItem.setProductId(i++);
        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(orderItem);
//        orderItemList.add(orderItem);
//        orderItemList.add(orderItem);
//        orderItemList.add(orderItem);
//        orderItemList.add(orderItem);
        this.orderItemList = orderItemList;
    }

    public List<OrderItem> getCartList() {
        return orderItemList;
    }

    public double getSubTotal() {
        try {
            double result = 0f;

            for (OrderItem item : orderItemList) {
                //            result += item.getCost();
            }
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1f;
        }
    }

    public int getOrderListSize() {
        try {
            return orderItemList.size();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }
}