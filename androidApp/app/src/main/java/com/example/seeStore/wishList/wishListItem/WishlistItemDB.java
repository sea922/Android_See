package com.example.seeStore.wishList.wishListItem;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.seeStore.wishList.wishListItem.WishlistItem;
import com.example.seeStore.wishList.wishListItem.WishlistItemDao;

@Database(entities = {WishlistItem.class}, version = 1)
public abstract class WishlistItemDB extends RoomDatabase {
    private static final String WISHLIST_ITEM_DB = "wishlist_item.db";

    public abstract WishlistItemDao wishlistItemDao();

    private static com.example.seeStore.wishList.wishListItem.WishlistItemDB wishlistItemDB;

    public static com.example.seeStore.wishList.wishListItem.WishlistItemDB with(Context context) {
        if (wishlistItemDB == null) {
            wishlistItemDB = Room.databaseBuilder(context.getApplicationContext(), com.example.seeStore.wishList.wishListItem.WishlistItemDB.class, WISHLIST_ITEM_DB).allowMainThreadQueries().build();
        }
        return wishlistItemDB;
    }
}