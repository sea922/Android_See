package com.example.mobile_scratch.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import com.example.mobile_scratch.adapter.ProductDetailAdapter;
import com.example.mobile_scratch.fragments.CategoryFragment;
import com.example.mobile_scratch.models.ProductModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;


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

    ViewPager imageViewPager;

    RadioGroup sizeGroup;

    RadioButton checkedButton;

    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        imageViewPager = findViewById(R.id.productImagePager);
        db = FirebaseFirestore.getInstance();
        product = getIntent().getParcelableExtra("product");

        ProductDetailAdapter productDetailAdapter = new ProductDetailAdapter(ProductDetailActivity.this, product.getImg());

        imageViewPager.setAdapter(productDetailAdapter);
        sizeGroup = findViewById(R.id.sizeGroup);


        inflater =  LayoutInflater.from(this);

        product.getSize().forEach(size -> {

            Button radioButton = (Button) inflater.inflate(R.layout.button_size, sizeGroup, false);
            radioButton.setText(Double.toString(size));

            sizeGroup.addView(radioButton);
        });


        ImageButton leftTopBarBtn = findViewById(R.id.leftTopBarBtn);

//        leftTopBarBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setContentView(R.layout.activity_main);
//                // Create an instance of the CategoryFragment
//                CategoryFragment categoryFragment = new CategoryFragment();
//
//                // Start a fragment transaction
//                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//
//                transaction.replace(R.id.fragment_container, categoryFragment); // Replace fragment_container with the ID of your container view
//
//                // Commit the transaction
//                transaction.commit();
//            }
//        });

        ImageButton backButton = findViewById(R.id.leftTopBarBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish the current activity and navigate back to the previous activity
            }
        });







        sizeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                for (int i=0;i<radioGroup.getChildCount();i++) {
                    RadioButton btn = (RadioButton) radioGroup.getChildAt(i);
                    if (!btn.isChecked()) {
                        //RadioButton btn = (RadioButton) radioGroup.getChildAt(i);
                        btn.setBackground(getDrawable(R.drawable.unchecked));
                        Log.d("unchecked", Integer.toString(i));
                    } else {
                        btn.setBackground(getDrawable(R.drawable.checked));
                        Log.d("checked", Integer.toString(i));
                    }
                }
            }
        });




    }

    @Override
    protected void onStop() {
        super.onStop();
        sizeGroup.clearCheck();
        Log.d("product detail", "stopped");
    }

    private void bindView() {

    }


//    private void bindData(DocumentSnapshot document) {
//        name = document.
//    }

}