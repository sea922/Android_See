package com.example.mobile_scratch.activity;

import android.os.Bundle;

import com.example.mobile_scratch.adapter.ProductDetailAdapter;
import com.example.mobile_scratch.models.CartItem;
import com.example.mobile_scratch.models.ProductModel;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.example.mobile_scratch.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProductDetailActivity extends AppCompatActivity {

    ProductModel product;


    FirebaseFirestore db;

    ViewPager imageViewPager;

    RadioGroup sizeGroup;


    LayoutInflater inflater;

    private int quantity = 1; // Default

    private ImageView[] dots;

    LinearLayout indicators;

    int imgCount;

    String user;

    DocumentReference cartRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        cartRef = db.collection("cart").document(user);

        imageViewPager = findViewById(R.id.productImagePager);

        product = getIntent().getParcelableExtra("product");
//        Log.d("product in detail", product.getDesc());
        ProductDetailAdapter productDetailAdapter = new ProductDetailAdapter(ProductDetailActivity.this, product.getImg());

        imageViewPager.setAdapter(productDetailAdapter);
        sizeGroup = findViewById(R.id.sizeGroup);


        inflater = LayoutInflater.from(this);

        product.getSize().forEach(size -> {

            Button radioButton = (Button) inflater.inflate(R.layout.button_size, sizeGroup, false);
            radioButton.setText(Double.toString(size));

            sizeGroup.addView(radioButton);
        });




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
//                        Log.d("unchecked", Integer.toString(i));
                    } else {
                        btn.setBackground(getDrawable(R.drawable.checked));
//                        Log.d("checked", Integer.toString(i));
                    }
                }
            }
        });
        bindView();
        bindIndicators();

    }


    private  void bindIndicators() {
        indicators = findViewById(R.id.pagerIndicator);
        imgCount = product.getImg().size();
        dots = new ImageView[imgCount];
        dots[0] = new ImageView(this);
        dots[0].setImageDrawable(getDrawable(R.drawable.sh_img_indicator_active_dot));
        indicators.addView(dots[0]);
        for (int i=1; i<imgCount;i++) {
            ImageView dot = new ImageView(this);
           dot.setImageDrawable(getDrawable(R.drawable.sh_img_indicator_inactive_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 0, 10, 0);
            dot.setLayoutParams(params);
           dots[i] = dot;
           indicators.addView(dot);
        }
        imageViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                dots[position].setImageDrawable(getDrawable(R.drawable.sh_img_indicator_active_dot));
                for(int i=0;i<imgCount;i++) {
                    if (i!=position) {
                        dots[i].setImageDrawable(getDrawable(R.drawable.sh_img_indicator_inactive_dot));
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
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
//        Log.d("slt size", selectedSize);
        if (selectedSize.isEmpty()) {
            // No size selected, display an error message or handle it as needed
            Snackbar.make(getWindow().getDecorView(), "Please select a size", Snackbar.LENGTH_SHORT).show();
            return;
        }
        cartItem.setSize(selectedSize);


        cartRef.get().addOnSuccessListener(task-> {
           if(!task.exists()) {
               //user does not have cart yet, create new cart document
               Map<String, String> dummy = new HashMap<>();
               dummy.put("date", new Timestamp(System.currentTimeMillis()).toString());
               cartRef.set(dummy);
           }
        });


        CollectionReference productRef = cartRef.collection(cartItem.getProductId());
        productRef.get().addOnSuccessListener(task -> {
           if (!task.isEmpty()) {
               //user is having added product in cart
               List<DocumentSnapshot> docs = task.getDocuments();
               for(int i = 0;i< docs.size();i++){
                    DocumentSnapshot doc = docs.get(i);
                    if (cartItem.getSize().equals(doc.getId())) {
                        //same product, same size -> increase quantity
                       int newQty = cartItem.getQuantity() + Integer.valueOf(doc.get("quantity").toString());
                       productRef.document(doc.getId()).update("quantity",newQty );
                       Snackbar.make(getWindow().getDecorView(), "Product quantity increased in cart", Snackbar.LENGTH_SHORT).show();
                       return;
                   }
               };
                //same product, different size -> add new variant document
               Map<String, Number> newVariantData = new HashMap<>();
               newVariantData.put("quantity", cartItem.getQuantity());
               newVariantData.put("price", cartItem.getPrice());
               productRef.document(cartItem.getSize()).set(newVariantData, SetOptions.merge());
               Snackbar.make(getWindow().getDecorView(), "Product variant added into cart", Snackbar.LENGTH_SHORT).show();
           } else {
               //user is NOT having added product in cart -> create new collection for added product
               Map<String, Number> newVariantData = new HashMap<>();
               newVariantData.put("quantity", cartItem.getQuantity());
               newVariantData.put("price", cartItem.getPrice());
               cartRef
                       .collection(cartItem.getProductId())
                       .document(cartItem.getSize())
                       .set(newVariantData, SetOptions.merge());
               Snackbar.make(getWindow().getDecorView(), "New product added into cart", Snackbar.LENGTH_SHORT).show();
           }
        }).addOnFailureListener(e -> {
            Snackbar.make(
                    getWindow().getDecorView(),
                    String.format("Failed to add product to cart, cause by %s", e.getMessage()),
                    Snackbar.LENGTH_LONG
            ).show();
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

    }
}