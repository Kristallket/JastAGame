package com.example.jg;

public class ShopItem {
    private int imageResourceId;
    private String name;
    private int price;
    private boolean isPurchased;
    private String itemId;

    public ShopItem(int imageResourceId, String name, int price, String itemId) {
        this.imageResourceId = imageResourceId;
        this.name = name;
        this.price = price;
        this.itemId = itemId;
        this.isPurchased = false;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }

    public String getItemId() {
        return itemId;
    }
} 