package com.example.mobile_scratch.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.adapter.CartAdapter;
import com.example.mobile_scratch.fragments.HomeFragment;
import com.example.mobile_scratch.models.CartItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;

    String user;
    FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cart);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartItems = new ArrayList<>();
//        cartAdapter = new CartAdapter(this, cartItems);
        recyclerView.setAdapter(cartAdapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        loadCartItems();

    }

    private void loadCartItems() {
        String path = String.format("cart/%s", user);
        Log.d("path", path);
        DocumentReference holeCartRef = db.document(path);

        holeCartRef.get().addOnSuccessListener(task -> {
            if (!task.exists()) {
                return;
            }
            Map<String, Object> data = task.getData();
            data.forEach((key, value) -> {
                String[] variant = extractItemID(key);
                    Log.d("product ID", variant[0]);
                    Log.d("product size", variant[1]);
                    Log.d("value", value.toString());
            });
        });

    }

    private String[] extractItemID(String id) {
        String productID = id.trim().substring(0,20);
        String size = id.trim().substring(20);
        return new String[]{productID, size};
    }
}
