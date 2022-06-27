package com.faridkamizi.inventory.holders;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface ShopInventoryHolder extends InventoryHolder {

    public void onClick(InventoryClickEvent e);

}
