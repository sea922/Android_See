package com.example.mobile_scratch.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.module.AppGlideModule;
import com.example.mobile_scratch.R;
import com.example.mobile_scratch.holder.ProductViewHolder;
import com.example.mobile_scratch.models.ProductModel;

import com.example.mobile_scratch.ultis.GlideApp;
import com.example.mobile_scratch.ultis.MyAppGlideModule;
import com.example.mobile_scratch.view.Item;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {
    Context context;
    List<ProductModel> itemList;
    FirebaseStorage storageFB;

    public ProductAdapter(Context context, List<ProductModel> itemList) {
        this.context = context;
        this.itemList = itemList;
        storageFB = FirebaseStorage.getInstance();


    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
//        Glide.with(context).load(itemList.get(position).getImg()).into(holder.getItemImg());
        StorageReference imgURL = storageFB.getReferenceFromUrl(itemList.get(position).getImg());
        Log.d("imgURL", imgURL.toString());
        GlideApp
                .with(context)
                .load(storageFB.getReferenceFromUrl(imgURL.toString())).into(holder.getItemImg());
        holder.getItemName().setText(itemList.get(position).getName());
        holder.getItemPrice().setText(itemList.get(position).getPrice().toString());

        holder.setIsRecyclable(false);


     }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}
