package com.example.seeStore.provider;

import com.example.seeStore.model.Cart;

public class CartProvider {
    private static CartProvider instance = null;
    private Cart myCart;

    public static CartProvider getInstance() {
        if (instance == null) {
            synchronized (CartProvider.class) {
                if (instance == null) {
                    instance = new CartProvider();
                }
            }
        }
        return instance;
    }

    private CartProvider() {
        myCart = new Cart();
    }

    public Cart getMyCart() {
        return myCart;
    }

    // TODO: cart
}
