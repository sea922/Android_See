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
import android.widget.LinearLayout;
import android.widget.Switch;

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
import java.util.stream.Collectors;


public class CategoryFragment extends Fragment{

    RecyclerView productRecyclerView;
    ArrayList<ProductModel> itemList = new ArrayList<>();

    ProductAdapter productAdapter;

    FirebaseFirestore db;
    List<ProductModel> filteredItemList;

    LinearLayout categoryList;
    private View button;

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
        Bundle extras =  this.getArguments();
        Log.d("received", extras.toString());
        itemList = extras.getParcelableArrayList("products");

        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productRecyclerView = getView().findViewById(R.id.productsRecyclerView);
        Log.d("mockupItem", itemList.toString());
        productRecyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        filteredItemList = new ArrayList<ProductModel>(itemList);
        Log.d("adapter init data", filteredItemList.toString());
        productAdapter = new ProductAdapter(this.getContext(), (ArrayList<ProductModel>) filteredItemList);

        productRecyclerView.setAdapter(productAdapter);

//        categoryList = getView().findViewById(R.id.categoryList);
//        categoryList.setOnClickListener(this::onClick);

    }

    private void filterProductsBy(String category) {

        if (category.equals("all")) {
            filteredItemList.clear();
            filteredItemList.addAll(itemList);
            Log.v("fileted item now", filteredItemList.toString());
            productAdapter.notifyDataSetChanged();
            return;
        }
        ArrayList<ProductModel> temp = (ArrayList<ProductModel>) itemList.stream().filter(item -> category.equals(item.getCat())).collect(Collectors.toList());

        filteredItemList.clear();
        filteredItemList.addAll(temp);
        Log.v("fileted item now", filteredItemList.toString());
        productAdapter.notifyDataSetChanged();
    }

    public void onCategoryClicked(View button) {
        this.button = button;

        switch (button.getId()) {
            case R.id.filter:
                filterProductsBy("all");

                break;
            case R.id.male:
                filterProductsBy("male");
                break;
            case R.id.female:
                filterProductsBy("female");
                break;
            case R.id.kids:
                filterProductsBy("kids");
                break;
            case R.id.accessory:
                filterProductsBy("accessory");
                break;
        }

        //productAdapter.notifyDataSetChanged();
    }


}