package com.example.mobile_scratch.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ProductModel implements Parcelable{
    String productID;
    ArrayList<String> img;
    String name;
    Double price;

    String cat;

    ArrayList<Double> size;

    String desc;




    public ProductModel() {
    }

    public ProductModel(String productID, ArrayList<String> img, String name, Double price, String cat, ArrayList<Double> size, String desc) {
        this.productID = productID;
        this.img = img;
        this.name = name;
        this.price = price;
        this.cat = cat;
        this.size = size;
        this.desc = desc;
    }


    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public ArrayList<String> getImg() {
        return img;
    }

    public void setImg(ArrayList<String> img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public ArrayList<Double> getSize() {
        return size;
    }

    public void setSize(ArrayList<Double> size) {
        this.size = size;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.productID);
        dest.writeStringList(this.img);
        dest.writeString(this.name);
        dest.writeValue(this.price);
        dest.writeString(this.cat);
        dest.writeList(this.size);
        dest.writeString(this.desc);
    }

    public void readFromParcel(Parcel source) {
        this.productID = source.readString();
        this.img = source.createStringArrayList();
        this.name = source.readString();
        this.price = (Double) source.readValue(Double.class.getClassLoader());
        this.cat = source.readString();
        this.size = new ArrayList<Double>();
        source.readList(this.size, Double.class.getClassLoader());
        this.desc = source.readString();
    }

    protected ProductModel(Parcel in) {
        this.productID = in.readString();
        this.img = in.createStringArrayList();
        this.name = in.readString();
        this.price = (Double) in.readValue(Double.class.getClassLoader());
        this.cat = in.readString();
        this.size = new ArrayList<Double>();
        in.readList(this.size, Double.class.getClassLoader());
        this.desc = in.readString();
    }

    public static final Creator<ProductModel> CREATOR = new Creator<ProductModel>() {
        @Override
        public ProductModel createFromParcel(Parcel source) {
            return new ProductModel(source);
        }

        @Override
        public ProductModel[] newArray(int size) {
            return new ProductModel[size];
        }
    };
}
