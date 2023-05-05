package com.example.seeStore.adapter;

import android.annotation.SuppressLint;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.example.seeStore.controller.CartController;
import com.example.seeStore.interfaces.ChangeNumberItem;
import com.example.seeStore.R;
import com.example.seeStore.model.OrderItem;
import com.example.seeStore.model.Product;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<OrderItem> orderItemList;
    private CartController cartController;
    private ChangeNumberItem changeNumberItem;
    private Context parentContext;

    public CartAdapter(List<OrderItem> orderItemList, Context context, ChangeNumberItem changeNumItemsListener) {
        this.orderItemList = orderItemList;
        this.cartController = new CartController(context);
        this.changeNumberItem = changeNumItemsListener;
        this.parentContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_view_holder, parent, false);

        return new ViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        try {
            OrderItem orderItem = orderItemList.get(position);

            // TODO: change this product into the relevant product item to the order item
            Product product = new Product();
            product.setId(0l);
            List<String> images = new ArrayList<>();
            images.add("https://bizweb.sapocdn.net/100/438/408/products/akn5040-den-4.jpg?v=1668244848000");
            images.add("https://bizweb.sapocdn.net/100/438/408/products/akn5040-den-5-308a032a-f9a4-4fb3-b73b-348e31c695db.jpg?v=1669013097000");
            product.setImages(images);
            product.setSex("nu");
            product.setCategory("ao-khoac-nu");
            product.setCost(499000l);
            product.setInventory(109);
            product.setName("Áo quần");

            holder.itemName.setText(product.getName());
            holder.itemCost.setText(String.valueOf(product.getCost()));
            holder.numItems.setText(String.valueOf(orderItem.getQuantity()));

//            Context itemViewContext = holder.itemView.getContext();
//            int imageResource = itemViewContext.getResources()
//                    .getIdentifier(product.getImages().get(0), null, itemViewContext.getPackageName());

            String imageUri = product.getImages().get(0);

            Glide.with(parentContext).load(imageUri).centerCrop().placeholder(R.drawable.item_placeholder).into(holder.itemImageUri);
            holder.incItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                cartController.increaseNumItems(garmentList, position, new ChangeNumItemsListener() {
                    //                    @Override
                    //                    public void onChanged() {
                    //                        changeNumItemsListener.onChanged();
                    //                    }
                    //                });
                }
            });

            holder.decItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                cartController.decreaseNumItems(garmentList, position, new ChangeNumItemsListener() {
                    //                    @Override
                    //                    public void onChanged() {
                    //                        changeNumItemsListener.onChanged();
                    //                    }
                    //                });
                }
            });

            holder.removeItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                cartManager.removeItem(garmentList, position, new ChangeNumItemsListener() {
                    //                    @Override
                    //                    public void onChanged() {
                    //                        changeNumItemsListener.onChanged();
                    //                    }
                    //                });
                }
            });
        } catch (NullPointerException e) {
            System.out.println("Garment list is empty");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        try {
            return orderItemList.size();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemCost, numItems;
        ImageView itemImageUri, incItem, decItem, removeItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.cartViewHolderName);
            itemCost = itemView.findViewById(R.id.itemsCost);
            itemImageUri = itemView.findViewById(R.id.cartViewHolderImage);
            numItems = itemView.findViewById(R.id.cartNumItemsTextView);
            incItem = itemView.findViewById(R.id.cartItemIncrease);
            decItem = itemView.findViewById(R.id.cartItemDecrease);
            removeItem = itemView.findViewById(R.id.cartItemRemoveBtn);
        }
    }
}