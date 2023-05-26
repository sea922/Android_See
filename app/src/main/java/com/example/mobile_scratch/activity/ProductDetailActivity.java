package com.example.mobile_scratch.activity;

import android.os.Bundle;

import com.example.mobile_scratch.models.ProductModel;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;


import com.example.mobile_scratch.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailActivity extends AppCompatActivity {

    ProductModel product;

    String name;

    Double price;

    Double[] size;

    String desc;

    TextView test;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        db = FirebaseFirestore.getInstance();
        product = getIntent().getParcelableExtra("product");
        //test = findViewById(R.id.productDetailID);

    }

    private void bindView() {

    }
//    private void bindData(DocumentSnapshot document) {
//        name = document.
//    }

}