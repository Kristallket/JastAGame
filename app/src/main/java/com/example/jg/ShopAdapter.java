package com.example.jg;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {
    private List<ShopItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onBuyClick(ShopItem item);
    }

    public ShopAdapter(List<ShopItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        ShopItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImage;
        private TextView itemName;
        private Button buyButton;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            buyButton = itemView.findViewById(R.id.buyButton);
        }

        public void bind(final ShopItem item, final OnItemClickListener listener) {
            itemImage.setImageResource(item.getImageResourceId());
            itemName.setText(item.getName());
            
            if (item.isPurchased()) {
                buyButton.setText("Куплено");
                buyButton.setEnabled(false);
            } else {
                buyButton.setText("Купить за " + item.getPrice());
                buyButton.setEnabled(true);
                buyButton.setOnClickListener(v -> listener.onBuyClick(item));
            }
        }
    }
} 