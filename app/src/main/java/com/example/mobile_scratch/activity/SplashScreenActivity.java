package com.example.mobile_scratch.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //delay splash by android default
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();


        //to delay splash screen
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startActivity(intent);
//                finish();
//            }
//        }, 3000);


    }
}