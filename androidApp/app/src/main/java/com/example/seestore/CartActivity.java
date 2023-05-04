package com.example.seestore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class CartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash );

        new Handler().postDelayed((Runnable) () -> {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}