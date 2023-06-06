package com.example.mobile_scratch.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.holder.ProductViewHolder;
import com.example.mobile_scratch.models.ProductModel;

import com.example.mobile_scratch.ultis.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {
    Context context;
    ArrayList<ProductModel> rootItemList;

    ArrayList<ProductModel> filteredItemList;
    FirebaseStorage storageFB;



    public ProductAdapter(Context context, ArrayList<ProductModel> rootItemList) {
        this.context = context;
        this.rootItemList = rootItemList;
        storageFB = FirebaseStorage.getInstance();


    }

    public void updateData(ArrayList<ProductModel> newData) {
        rootItemList.clear();
        rootItemList.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ProductViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

//        Glide.with(context).load(itemList.get(position).getImg()).into(holder.getItemImg());
        StorageReference imgURL = storageFB.getReferenceFromUrl(rootItemList.get(position).getImg().get(0));
        Log.d("adapter now", rootItemList.toString());
        GlideApp
                .with(context)
                .load(storageFB.getReferenceFromUrl(imgURL.toString())).into(holder.getItemImg());
//        holder.getItemName().setText(rootItemList.get(position).getName());
//        holder.getItemPrice().setText(rootItemList.get(position).getPrice().toString());

        holder.setProduct(rootItemList.get(position));
        holder.setIsRecyclable(false);


     }



    @Override
    public int getItemCount() {
        return rootItemList.size();
    }


}
