package com.example.jg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment implements ShopAdapter.OnItemClickListener {
    private static final String PREFS_NAME = "GameSettings";
    private static final String PURCHASED_ITEMS_KEY = "purchasedItems";
    private List<ShopItem> shopItems;
    private ShopAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.shopRecyclerView);
        MaterialButton backButton = view.findViewById(R.id.backButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Инициализация списка предметов
        initializeShopItems();
        
        // Загрузка состояния покупок
        loadPurchasedItems();
        
        adapter = new ShopAdapter(shopItems, this);
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void initializeShopItems() {
        shopItems = new ArrayList<>();
        // Здесь можно добавлять предметы в магазин
        addShopItem(R.drawable.box, "Улучшенная коробка", 100, SettingsFragment.BOX_UPGRADE_ID);
        addShopItem(R.drawable.box, "Золотая коробка", 500, SettingsFragment.GOLD_BOX_ID);
        addShopItem(R.drawable.box, "Алмазная коробка", 1000, SettingsFragment.DIAMOND_BOX_ID);
    }

    public void addShopItem(int imageResourceId, String name, int price, String itemId) {
        shopItems.add(new ShopItem(imageResourceId, name, price, itemId));
    }

    private void loadPurchasedItems() {
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        String purchasedItems = settings.getString(PURCHASED_ITEMS_KEY, "");
        
        for (ShopItem item : shopItems) {
            if (purchasedItems.contains(item.getItemId())) {
                item.setPurchased(true);
            }
        }
    }

    private void savePurchasedItem(String itemId) {
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        String purchasedItems = settings.getString(PURCHASED_ITEMS_KEY, "");
        
        if (!purchasedItems.contains(itemId)) {
            purchasedItems += itemId + ",";
            settings.edit().putString(PURCHASED_ITEMS_KEY, purchasedItems).apply();
        }
    }

    @Override
    public void onBuyClick(ShopItem item) {
        int totalScore = SettingsFragment.getTotalScore(getContext());
        
        if (totalScore >= item.getPrice()) {
            new AlertDialog.Builder(getContext())
                .setTitle("Подтверждение покупки")
                .setMessage("Вы хотите купить " + item.getName() + " за " + item.getPrice() + " очков?")
                .setPositiveButton("Да", (dialog, which) -> {
                    // Вычитаем очки
                    SettingsFragment.updateTotalScore(getContext(), -item.getPrice());
                    // Отмечаем предмет как купленный
                    item.setPurchased(true);
                    savePurchasedItem(item.getItemId());
                    // Обновляем список
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Нет", null)
                .show();
        } else {
            new AlertDialog.Builder(getContext())
                .setTitle("Недостаточно очков")
                .setMessage("У вас недостаточно очков для покупки этого предмета.")
                .setPositiveButton("OK", null)
                .show();
        }
    }

    public static boolean isItemPurchased(Context context, String itemId) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        String purchasedItems = settings.getString(PURCHASED_ITEMS_KEY, "");
        return purchasedItems.contains(itemId);
    }
} 