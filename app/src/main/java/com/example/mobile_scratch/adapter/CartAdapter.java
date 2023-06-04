package com.example.mobile_scratch.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.models.CartItem;
import com.example.mobile_scratch.ultis.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> cartItems;

    private Context mContext;

    FirebaseStorage storageFB;

    public CartAdapter(Context mContext, List<CartItem> cartItems) {
        this.cartItems = cartItems;
        this.mContext = mContext;
        this.storageFB = FirebaseStorage.getInstance();
        Log.d("receive cart items", cartItems.toString());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to the ViewHolder
        CartItem cartItem = cartItems.get(position);
        Log.d("carItem binding name", cartItem.getProductName());
        StorageReference imgURL = storageFB.getReferenceFromUrl(cartItem.getImg());


        holder.textViewProductName.setText(cartItem.getProductName());
        holder.textViewPrice.setText(String.valueOf(cartItem.getPrice()));
        holder.textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));


        GlideApp
                .with(mContext)
                .load(storageFB.getReferenceFromUrl(imgURL.toString())).into(holder.cartItemImg);




        holder.cartItemDelete.setOnClickListener(view -> {
            cartItems.remove(holder.getBindingAdapterPosition());
            notifyItemRemoved(holder.getBindingAdapterPosition());
            notifyItemRangeChanged(holder.getBindingAdapterPosition(), cartItems.size());
        });

        holder.cartItemDec.setOnClickListener(view -> {
            cartItem.setQuantity(cartItem.getQuantity()-1);
            holder.textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));
        });

        holder.cartItemInc.setOnClickListener(view->{
            cartItem.setQuantity(cartItem.getQuantity()+1);
            holder.textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

//    private void decreaseItemDB()
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductName;
         TextView textViewPrice;
         TextView textViewQuantity;

         ImageView cartItemImg;

         ImageButton cartItemDelete, cartItemInc, cartItemDec;

         TextView cartItemSize;


        public ViewHolder(View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.cartItemName);
            textViewPrice = itemView.findViewById(R.id.cartItemPrice);
            textViewQuantity = itemView.findViewById(R.id.cartItemQuantity);
            cartItemImg = itemView.findViewById(R.id.cartItemImage);
            cartItemInc = itemView.findViewById(R.id.cartItemIncrease);
            cartItemDec = itemView.findViewById(R.id.cartItemDecrease);
            cartItemDelete = itemView.findViewById(R.id.cartItemDelete);
            cartItemSize = itemView.findViewById(R.id.cartItemSize);
        }
    }
}
