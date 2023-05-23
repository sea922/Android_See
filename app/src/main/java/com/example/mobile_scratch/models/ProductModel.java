package com.example.mobile_scratch.models;

import com.google.firebase.firestore.DocumentReference;

public class ProductModel {
    DocumentReference catID;
    String img;
    String name;
    Double price;

    String cat;

    public ProductModel() {
    }

    public ProductModel(DocumentReference catID, String img, String name, Double price, String cat) {
        this.catID = catID;
        this.img = img;
        this.name = name;
        this.price = price;
        this.cat = cat;
    }

    public DocumentReference getCatID() {
        return catID;
    }

    public void setCatID(DocumentReference catID) {
        this.catID = catID;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
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
}
