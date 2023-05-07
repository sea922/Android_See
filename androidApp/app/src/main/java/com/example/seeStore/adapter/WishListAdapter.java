package com.example.seeStore.adapter;

import static com.example.seeStore.utils.StringUtils.vndFormatPrice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.seeStore.R;
import com.example.seeStore.activity.ProductDetailActivity;
import com.example.seeStore.fragment.WishListFragment;
import com.example.seeStore.interfaces.ChangeNumberItem;
import com.example.seeStore.model.Product;

import com.example.seeStore.wishList.wishListItem.WishlistItem;
import com.example.seeStore.wishList.WishListController;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.ViewHolder> {
    private final List<WishlistItem> wishlistItemList;
    private final List<Product> productList;
    private final WishListController wishlistController;
    private final Context context;
    private final ChangeNumberItem changeNumItemsListener;

    public WishListAdapter(WishListController wishlistController, Context context, ChangeNumberItem changeNumItemsListener) {
        this.wishlistItemList = wishlistController.getWishlistItemList();
        this.productList = wishlistController.getProductList();
        this.wishlistController = wishlistController;
        this.context = context;
        this.changeNumItemsListener = changeNumItemsListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.wish_list_item, parent, false);

        return new ViewHolder(inflater);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Product product = productList.get(position);
        WishlistItem item = wishlistItemList.get(position);

        holder.itemName.setText(product.getName());
        holder.itemPrice.setText(vndFormatPrice(product.getPrice()));
        Glide.with(context)
                .load(productList.get(position).getFirstImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.item_placeholder)
                .into(holder.itemImage);
    }

    @Override
    public int getItemCount() {
        return wishlistItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemPrice;
        ImageView itemImage;
        //        TextView itemRemoveBtn;
        MaterialButton itemRemoveBtn;
        LinearLayout itemWrapper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.wishlistViewHolderName);
            itemPrice = itemView.findViewById(R.id.wishlistViewHolderPrice);
            itemImage = itemView.findViewById(R.id.wishlistViewHolderImage);
            itemRemoveBtn = itemView.findViewById(R.id.wishlistViewHolderRemoveBtn);
            itemWrapper = itemView.findViewById(R.id.wishlistViewHolderWrapper);
            setEvents();
        }

        private void setEvents() {
            itemRemoveBtn.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(View view) {
                    wishlistController.deleteItem(getAdapterPosition(), new ChangeNumberItem() {
                        @Override
                        public void onChanged() {
                            notifyDataSetChanged();
                            changeNumItemsListener.onChanged();
                        }
                    });
                }
            });

            itemWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Product productItem = productList.get(getAdapterPosition());
                    Intent intent = new Intent(context, ProductDetailActivity.class);
                    intent.putExtra("productId", productItem.getId());
                    intent.putExtra("previousFragment", WishListFragment.TAG);
                    context.startActivity(intent);
                }
            });
        }
    }
}