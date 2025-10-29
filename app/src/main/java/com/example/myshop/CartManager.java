package com.example.myshop;

import com.example.myshop.Models.CartModel;

import java.util.ArrayList;

public class CartManager {
    private static ArrayList<CartModel> cartList = new ArrayList<>();

    public static void addToCart(CartModel item) {
        for(CartModel cartItem : cartList){
            if(cartItem.getName().equals(item.getName())){
                cartItem.increaseQuantity();
                return;
            }
        }
        cartList.add(item);
    }

    public static ArrayList<CartModel> getCart() {
        return cartList;
    }

    public static void clearCart() {
        cartList.clear();
    }
}
