package com.example.mobile_scratch.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> cartItems;

    public CartAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to the ViewHolder
        CartItem cartItem = cartItems.get(position);
        holder.textViewProductName.setText(cartItem.getProductName());
        holder.textViewPrice.setText(String.valueOf(cartItem.getPrice()));
        holder.textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewProductName;
        public TextView textViewPrice;
        public TextView textViewQuantity;

        public ViewHolder(View itemView) {
            super(itemView);
//            textViewProductName = itemView.findViewById(R.id.textViewProductName);
//            textViewPrice = itemView.findViewById(R.id.textViewPrice);
//            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
        }
    }
}
