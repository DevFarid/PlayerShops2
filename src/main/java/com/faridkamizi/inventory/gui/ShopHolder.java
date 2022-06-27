package com.faridkamizi.inventory.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface ShopHolder extends InventoryHolder {

    public void onClick(InventoryClickEvent e);

}
