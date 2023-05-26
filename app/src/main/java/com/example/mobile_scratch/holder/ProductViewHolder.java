package com.example.mobile_scratch.holder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.activity.ProductDetailActivity;
import com.example.mobile_scratch.models.ProductModel;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    ImageView itemImg;
    TextView itemName, itemPrice;

    LinearLayout wrapper;

    ProductModel product;

    Context context;
    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);
        itemImg= itemView.findViewById(R.id.itemImg);
        itemName= itemView.findViewById(R.id.itemName);
        itemPrice= itemView.findViewById(R.id.itemPrice);
        wrapper= itemView.findViewById(R.id.productWrapper);
        context = itemView.getContext();
        wrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetailActivity.class);

                intent.putExtra("product", product);
                context.startActivity(intent);
            }
        });
    }


    public ImageView getItemImg() {
        return itemImg;
    }

//    public TextView getItemName() {
//        return itemName;
//    }
//
//    public TextView getItemPrice() {
//        return itemPrice;
//    }

    public void setProduct(ProductModel product) {
        this.product = product;
        itemName.setText(product.getName());
        itemPrice.setText(product.getPrice().toString());

    }


}
