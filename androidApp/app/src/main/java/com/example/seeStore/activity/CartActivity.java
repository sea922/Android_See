package com.example.seeStore.activity;

import android.annotation.SuppressLint;
import com.example.seeStore.adapter.CartAdapter;
import com.example.seeStore.controller.CartController;
import com.example.seeStore.interfaces.ChangeNumberItem;
import com.example.seeStore.R;
import com.example.seeStore.utils.StringUtils;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CartActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapter;
    private RecyclerView cartRecyclerView;
    private CartController cartController;

    TextView subTotalTextView, shippingTextView, totalTextView;

    private long total;
    private LinearLayout cartScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartController = new CartController(this);

        initView();
        initCartList();
        renderCart();
    }

    private void initView() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        cartScrollView = findViewById(R.id.cartScrollView);
        subTotalTextView = findViewById(R.id.subTotalValueCartTextView);
        shippingTextView = findViewById(R.id.shippingValueCartTextView);
        totalTextView = findViewById(R.id.totalValueCartTextView);
    }

    private void initCartList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        cartRecyclerView.setLayoutManager(linearLayoutManager);

        adapter = new CartAdapter(cartController.getCartList(), this, new ChangeNumberItem() {
            @Override
            public void onChanged() {
                renderCart();
            }
        });

        cartRecyclerView.setAdapter(adapter);
        if (cartController.getCartList() != null && cartController.getCartList().isEmpty()) {
            // TODO do something when cart is empty
        }
    }

    @SuppressLint("SetTextI18n")
    private void renderCart() {
        long subTotal = cartController.getSubTotal();
        long shippingCost = (subTotal == -1) ? 0 : 15000;
        total = subTotal + shippingCost;

        subTotalTextView.setText(StringUtils.long2money(subTotal) + " VND");
        shippingTextView.setText(StringUtils.long2money(shippingCost) + " VND");
        totalTextView.setText(StringUtils.long2money(total) + "VND");


    }
}