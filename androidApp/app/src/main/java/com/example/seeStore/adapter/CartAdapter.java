package com.example.seeStore.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import com.example.seeStore.controller.CartController;
import com.example.seeStore.interfaces.ChangeNumberItem;
import com.example.seeStore.R;
import com.example.seeStore.model.Product;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<Product> productList;
    private CartController cartController;
    private ChangeNumberItem changeNumItemsListener;

    public CartAdapter(List<Product> garmentList, Context context, ChangeNumberItem changeNumItemsListener) {
        this.productList = productList;
        this.cartController = new CartController(context);
        this.changeNumItemsListener = changeNumItemsListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_view_holder, parent, false);

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        try {
            Product garment = productList.get(position);
            holder.itemName.setText(garment.getName());
            holder.itemCost.setText(String.valueOf(garment.getCost()));
            holder.numItems.setText(String.valueOf(cartController.getOrderListSize()));

            Context itemViewContext = holder.itemView.getContext();
            int drawableResource = itemViewContext.getResources()
                    .getIdentifier(garment.getImages().get(0), "drawable", holder.itemView.getContext().getPackageName());


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
            return productList.size();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemCost, numItems;
        ImageView itemUri, incItem, decItem, removeItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.cartViewHolderName);
            itemCost = itemView.findViewById(R.id.itemsCost);
            itemUri = itemView.findViewById(R.id.cartViewHolderImage);
            numItems = itemView.findViewById(R.id.cartNumItemsTextView);
            incItem = itemView.findViewById(R.id.cartItemIncrease);
            decItem = itemView.findViewById(R.id.cartItemDecrease);
            removeItem = itemView.findViewById(R.id.cartItemRemoveBtn);
        }
    }
}