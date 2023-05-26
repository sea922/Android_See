package com.example.mobile_scratch.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.fragments.CategoryFragment;
import com.example.mobile_scratch.fragments.HomeFragment;
import com.example.mobile_scratch.fragments.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button logout;
    FirebaseAuth simpleAuth;
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        logout = findViewById(R.id.logout);

        HomeFragment homeFragment = new HomeFragment();
        CategoryFragment categoryFragment = new CategoryFragment();
        MapFragment mapFragment = new MapFragment();

        bottomNavigationView = findViewById(R.id.mainBottomNavBar);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mainNavBarHomeBtn:
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.mainFragmentContainer, homeFragment)
                                .commit();
                        return true;
//                    case R.id.mainNavBarCategoryBtn:
//                        getSupportFragmentManager()
//                                .beginTransaction()
//                                .replace(R.id.mainFragmentContainer, categoryFragment)
//                                .commit();
//                        return true;
                    case R.id.mainNavBarMapBtn:
                        startActivity(new Intent(MainActivity.this, MapActivity.class));
                        return true;


                }
                return false;
            }
        });
        simpleAuth = FirebaseAuth.getInstance();
//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                simpleAuth.signOut();
//                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
//                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//                finish();
//            }
//
//        });
    }


}