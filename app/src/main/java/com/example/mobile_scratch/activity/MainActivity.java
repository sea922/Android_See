package com.example.mobile_scratch.activity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.adapter.ProductAdapter;
import com.example.mobile_scratch.fragments.CartFragment;
import com.example.mobile_scratch.fragments.CategoryFragment;
import com.example.mobile_scratch.fragments.HomeFragment;
import com.example.mobile_scratch.fragments.UserFragment;
import com.example.mobile_scratch.models.ProductModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button logout;
    FirebaseAuth simpleAuth;
    BottomNavigationView bottomNavigationView;

    ArrayList<ProductModel> itemList = new ArrayList<>();
    FirebaseFirestore db;

    GoogleSignInClient googleSignInClient;

    HomeFragment homeFragment;
    CategoryFragment categoryFragment;

    UserFragment userFragment;

    CartFragment cartFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //logout = findViewById(R.id.logout);
        db = FirebaseFirestore.getInstance();
        itemList.clear();
        homeFragment = new HomeFragment();
        categoryFragment = new CategoryFragment();

        userFragment = new UserFragment();

        cartFragment = new CartFragment();

        Bundle bundle = new Bundle();

        bottomNavigationView = findViewById(R.id.mainBottomNavBar);

        db.collection("products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                ProductModel productModel = document.toObject(ProductModel.class);
                                productModel.setProductID(document.getId());
                                itemList.add(productModel);
                            }
                            bundle.putParcelableArrayList("products", itemList);
                            categoryFragment.setArguments(bundle);
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
                                        case R.id.mainNavBarCategoryBtn:
//                        Log.d("which args", categoryFragment.getArguments().toString());
                                            getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .replace(R.id.mainFragmentContainer, categoryFragment)
                                                    .commit();
                                            return true;
                                        case R.id.mainNavBarMapBtn:
                                            startActivity(new Intent(MainActivity.this, MapActivity.class));
                                            return true;
//                                        case R.id.mainNavBarProfileBtn:
//                                            startActivity(new Intent(MainActivity.this, UserActivity.class));
//                                            return true;
                                        case R.id.mainNavBarProfileBtn:
                                            getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .replace(R.id.mainFragmentContainer, userFragment)
                                                    .commit();
                                            return true;

                                        case R.id.mainNavBarCartBtn:Btn:
                                            getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .replace(R.id.mainFragmentContainer, cartFragment)
                                                    .commit();
                                            return true;
                                    }
                                    return false;
                                }
                            });
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });



        simpleAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);

//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        // Check condition
//                        if (task.isSuccessful()) {
//                            // When task is successful sign out from firebase
//                            simpleAuth.signOut();
//                            // Display Toast
//                            Toast.makeText(getApplicationContext(), "Logout successful", Toast.LENGTH_SHORT).show();
//                            // Finish activity
//                            finish();
//                        }
//                    }
//                });
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

    public void onCategoryClicked(View button) {
        categoryFragment.onCategoryClicked(button);
    }

}