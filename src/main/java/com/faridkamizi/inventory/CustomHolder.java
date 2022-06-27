package com.faridkamizi.inventory;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class CustomHolder implements InventoryHolder {

    private final Map<Integer, Icon> icons = new HashMap<>();

    private final int size;
    private final String title;

    public CustomHolder(String title, int size) {
        this.title = title;
        this.size = size;
    }

    public void setIcon(int position, Icon icon) {
        this.icons.put(position, icon);
    }

    public Icon getIcon(int position) {
        return this.icons.get(position);
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, this.size, this.title);

        for(Map.Entry<Integer, Icon> entry : this.icons.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().itemStack);
        }
        return inventory;
    }
}
