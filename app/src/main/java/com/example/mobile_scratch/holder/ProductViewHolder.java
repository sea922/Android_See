package com.example.mobile_scratch.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_scratch.R;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    ImageView itemImg;
    TextView itemName, itemPrice;

    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);
        itemImg= itemView.findViewById(R.id.itemImg);
        itemName= itemView.findViewById(R.id.itemName);
        itemPrice= itemView.findViewById(R.id.itemPrice);

    }

    public ImageView getItemImg() {
        return itemImg;
    }

    public TextView getItemName() {
        return itemName;
    }

    public TextView getItemPrice() {
        return itemPrice;
    }
}
