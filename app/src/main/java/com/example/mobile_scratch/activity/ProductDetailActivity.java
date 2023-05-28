package com.example.mobile_scratch.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import com.example.mobile_scratch.adapter.ProductDetailAdapter;
import com.example.mobile_scratch.fragments.CategoryFragment;
import com.example.mobile_scratch.models.CartItem;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailActivity extends AppCompatActivity {

    ProductModel product;


    FirebaseFirestore db;

    ViewPager imageViewPager;

    RadioGroup sizeGroup;


    LayoutInflater inflater;

    private int quantity = 1; // Default


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        imageViewPager = findViewById(R.id.productImagePager);

        product = getIntent().getParcelableExtra("product");
        Log.d("product in detail", product.getDesc());
        ProductDetailAdapter productDetailAdapter = new ProductDetailAdapter(ProductDetailActivity.this, product.getImg());

        imageViewPager.setAdapter(productDetailAdapter);
        sizeGroup = findViewById(R.id.sizeGroup);


        inflater = LayoutInflater.from(this);

        product.getSize().forEach(size -> {

            Button radioButton = (Button) inflater.inflate(R.layout.button_size, sizeGroup, false);
            radioButton.setText(Double.toString(size));

            sizeGroup.addView(radioButton);
        });


        ImageButton leftTopBarBtn = findViewById(R.id.leftTopBarBtn);

        ImageButton backButton = findViewById(R.id.leftTopBarBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish the current activity and navigate back to the previous activity
            }
        });

        TextView quantityTextView = findViewById(R.id.quantityTextView);
        Button decrementButton = findViewById(R.id.decrementButton);
        Button incrementButton = findViewById(R.id.incrementButton);

        // Set the initial
        quantityTextView.setText(String.valueOf(quantity));

        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1) {
                    quantity--;
                    quantityTextView.setText(String.valueOf(quantity));
                }
            }
        });

        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity++;
                quantityTextView.setText(String.valueOf(quantity));
            }
        });


        Button btnAddToCart = findViewById(R.id.btnAddToCart);
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });

        sizeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    RadioButton btn = (RadioButton) radioGroup.getChildAt(i);
                    if (!btn.isChecked()) {
                        btn.setBackground(getDrawable(R.drawable.unchecked));
                        Log.d("unchecked", Integer.toString(i));
                    } else {
                        btn.setBackground(getDrawable(R.drawable.checked));
                        Log.d("checked", Integer.toString(i));
                    }
                }
            }
        });
        bindView();

    }

//    private void fetchProductDetails() {
//        db.collection("products")
//                .document(product.getProductID())
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        name = documentSnapshot.getString("name");
//                        price = documentSnapshot.getDouble("price");
////                        size = documentSnapshot.get("size", Double[].class);
//                        desc = documentSnapshot.getString("desc");
//                        bindView();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Handle any errors
//                    Log.e("ProductDetailActivity", "Error getting product details: " + e.getMessage());
//                });
//    }

    private void bindView() {
        TextView productNameTextView = findViewById(R.id.productName);
        TextView productPriceTextView = findViewById(R.id.productPrice);
        TextView productDescTextView = findViewById(R.id.productDescription);

        productNameTextView.setText(product.getName());
        productPriceTextView.setText(product.getPrice() + "$");
        productDescTextView.setText(product.getDesc());
    }

    private void addToCart() {
        CartItem cartItem = new CartItem();
        cartItem.setProductId(product.getProductID());
        cartItem.setProductName(product.getName());
        cartItem.setPrice(product.getPrice());
        cartItem.setQuantity(quantity);
        String selectedSize = getSelectedSize();
        Log.d("slt size", selectedSize);
        if (selectedSize.isEmpty()) {
            // No size selected, display an error message or handle it as needed
            Snackbar.make(getWindow().getDecorView(), "Please select a size", Snackbar.LENGTH_SHORT).show();
            return;
        }
        cartItem.setSize(selectedSize);


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference cartRef = db.collection("cart");

        cartRef.add(cartItem)
                .addOnSuccessListener(documentReference -> {
                    Snackbar.make(getWindow().getDecorView(), "Product added to cart", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(getWindow().getDecorView(), "Failed to add product to cart", Snackbar.LENGTH_SHORT).show();
                });


    }
    private String getSelectedSize() {
        int selectedSizeId = sizeGroup.getCheckedRadioButtonId();
        if (selectedSizeId != -1) {
            RadioButton selectedSizeButton = findViewById(selectedSizeId);
            return selectedSizeButton.getText().toString();
        } else {
            return "";
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        sizeGroup.clearCheck();
        Log.d("product detail", "stopped");
    }
}