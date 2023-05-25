package com.example.mobile_scratch.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductModel implements Parcelable{
    String productID;
    String[] img;
    String name;
    Double price;

    String cat;

    double[] size;

    String desc;




    public ProductModel() {
    }

//    public ProductModel(String productID, String[] img, String name, Double price, String cat, double[] size, String desc) {
//        this.productID = productID;
//        this.img = img;
//        this.name = name;
//        this.price = price;
//        this.cat = cat;
//        this.size = size;
//        this.desc = desc;
//    }

    public ProductModel(Parcel in) {
        super();
        this.productID = in.readString();
        this.img = in.createStringArray();
        this.name = in.readString();
        this.price = in.readDouble();
        this.cat = in.readString();
        this.size = in.createDoubleArray();
        this.desc = in.readString();


    }

    public static  final Parcelable.Creator<ProductModel> CREATOR = new Parcelable.Creator<ProductModel>() {
        @Override
        public ProductModel createFromParcel(Parcel in) {
            return new ProductModel(in);
        }
        @Override
        public ProductModel[] newArray(int size) {
            return new ProductModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel in, int i) {
        in.writeString(this.productID);
        in.writeString(this.cat);
        in.writeString(this.name);
        in.writeDouble(this.price);
        in.writeStringArray(this.img);
        in.writeDoubleArray(this.size);
        in.writeString(this.desc);


    }




    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String[] getImg() {
        return img;
    }

    public void setImg(String[] img) {
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

    public double[] getSize() {
        return size;
    }

    public void setSize(double[] size) {
        this.size = size;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
