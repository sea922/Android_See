package com.example.mobile_scratch.adapter;

import android.app.Activity;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> cartItems;

    private Context mContext;

    FirebaseStorage storageFB;

    FirebaseFirestore db;

    String user;

    DocumentReference cartRef;

    TextView totalTextViewControl;

    AtomicDouble total;

    public CartAdapter(Context mContext, List<CartItem> cartItems, TextView totalTextView, AtomicDouble cartTotal) {
        this.cartItems = cartItems;
        this.mContext = mContext;
        storageFB = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        cartRef = db.collection("cart").document(user);
        totalTextViewControl = totalTextView;
        total = cartTotal;
//      updateTotalTextView(total.get());


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
        Activity fragmentAssociateActivity = (Activity) this.mContext;
        CartItem cartItem = cartItems.get(position);
        Log.d("carItem binding name", cartItem.getProductName());
        StorageReference imgURL = storageFB.getReferenceFromUrl(cartItem.getImg());

        holder.setIsRecyclable(false);
        holder.textViewProductName.setText(cartItem.getProductName());
        holder.textViewPrice.setText(String.valueOf(cartItem.getPrice()));
        holder.textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));
        holder.cartItemSize.setText(String.valueOf(cartItem.getSize()));

        GlideApp
                .with(mContext)
                .load(storageFB.getReferenceFromUrl(imgURL.toString())).into(holder.cartItemImg);


        holder.cartItemDelete.setOnClickListener(view -> {
            total.getAndAdd(-cartItem.getPrice()*cartItem.getQuantity());
            updateTotalTextView(total.get());
            cartItems.remove(holder.getAbsoluteAdapterPosition());
            FieldPath productFieldPath = FieldPath.of(cartItem.getProductId()+ cartItem.getSize());

            cartRef.update(productFieldPath, FieldValue.delete()).addOnSuccessListener(task->{
                notifyDataSetChanged();
                notifyItemRangeChanged(holder.getAbsoluteAdapterPosition(), cartItems.size());
                Snackbar.make(fragmentAssociateActivity.getWindow().getDecorView(), "Item deleted!", Snackbar.LENGTH_SHORT).show();
            });

        });

        holder.cartItemDec.setOnClickListener(view -> {
            int newQty = cartItem.getQuantity()-1;
            total.getAndAdd(-cartItem.getPrice());
            updateTotalTextView(total.get());
            if (newQty==0) {
                cartItems.remove(holder.getAbsoluteAdapterPosition());
                FieldPath productFieldPath = FieldPath.of(cartItem.getProductId()+ cartItem.getSize());

                cartRef.update(productFieldPath, FieldValue.delete()).addOnSuccessListener(task->{
                    notifyDataSetChanged();
                    notifyItemRangeChanged(holder.getAbsoluteAdapterPosition(), cartItems.size());
                    Snackbar.make(fragmentAssociateActivity.getWindow().getDecorView(), "Item deleted!", Snackbar.LENGTH_SHORT).show();
                });
                return;
            }
            cartItem.setQuantity(newQty);
            holder.textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));
            updateQtyDB(newQty, cartItem.getProductId(), cartItem.getSize(),fragmentAssociateActivity);

        });

        holder.cartItemInc.setOnClickListener(view->{
            int newQty = cartItem.getQuantity()+1;
            total.getAndAdd(cartItem.getPrice());
            updateTotalTextView(total.get());
            cartItem.setQuantity(newQty);

            holder.textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));
            updateQtyDB(newQty, cartItem.getProductId(), cartItem.getSize(), fragmentAssociateActivity);

        });
    }

    private void updateQtyDB(int newQty, String ProductID, String size, Activity fragmentView) {
        //FieldPath productFieldPath = FieldPath.of(ProductID+size);
        String productFieldPath = ProductID+size;
        FieldPath qtyFieldPath = FieldPath.of(productFieldPath, "quantity");
        cartRef.update(qtyFieldPath, newQty).addOnSuccessListener(task->{
            Snackbar.make(fragmentView.getWindow().getDecorView(), "Item quantity updated!", Snackbar.LENGTH_SHORT).show();
        });

    }

    private void updateTotalTextView(Double total) {
        Double round = Math.round(total * 100.0) / 100.0;
        totalTextViewControl.setText(String.format("%.2f", round));
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
