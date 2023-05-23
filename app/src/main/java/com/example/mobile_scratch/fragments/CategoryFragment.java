package com.example.mobile_scratch.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.adapter.ProductAdapter;
import com.example.mobile_scratch.models.ProductModel;
import com.example.mobile_scratch.ultis.GlideApp;
import com.example.mobile_scratch.view.Item;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class CategoryFragment extends Fragment {

    RecyclerView productRecyclerView;
    List<ProductModel> itemList = new ArrayList<>();

    ProductAdapter productAdapter;

    FirebaseFirestore db;
    List<ProductModel> filteredItemList = new ArrayList<>();

    public CategoryFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Todo: khi xai api thi sua lai type cua itemImg
//        itemList.add(new Item("Product 1", "30 USD", R.drawable.p1));
//        itemList.add(new Item("Product 2", "25 USD", R.drawable.p2));
//        itemList.add(new Item("Product 3", "60 USD", R.drawable.p3));
//        itemList.add(new Item("Product 4", "70 USD", R.drawable.p4));
//        itemList.add(new Item("Product 5", "30 USD", R.drawable.p1));
//        itemList.add(new Item("Product 6", "25 USD", R.drawable.p2));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        itemList.clear();
        db.collection("products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                ProductModel productModel = document.toObject(ProductModel.class);
                                itemList.add(productModel);
                                productAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productRecyclerView = getView().findViewById(R.id.productsRecyclerView);
        Log.d("mockupItem", itemList.toString());
        productRecyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        productAdapter = new ProductAdapter(this.getContext(), itemList);

        productRecyclerView.setAdapter(productAdapter);
    }
}