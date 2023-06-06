package com.example.mobile_scratch.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class CartItem implements Parcelable {
    private String productId;
    private String productName;
    private double price;
    private String size;
    private int quantity;

    private  String img;

    public CartItem() {

    }
    public  CartItem(Map<String, Object> item) {
        this.productId = item.get("productId").toString();
        this.price = (Double) item.get("price");
        this.quantity = (Integer) item.get("quantity");
        this.size = item.get("size").toString();
        this.productName = item.get("name").toString();
        this.img = item.get("img").toString();
    }
    protected CartItem(Parcel in) {
        productId = in.readString();
        productName = in.readString();
        price = in.readDouble();
        size = in.readString();
        quantity = in.readInt();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImg() {
        return img;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(productName);
        dest.writeDouble(price);
        dest.writeString(size);
        dest.writeInt(quantity);
    }
}
