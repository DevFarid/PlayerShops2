package com.faridkamizi.system;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class ShopLocation {

    /**
     * An arraylist which will describe a shop location by
     * index 0: clicked block where the shop was created.
     * index 1: the 2nd block where [+1 to x-coordinate] of index 0 cloned.
     * index 2: hologram title location
     * index 3: hologram viewer location
     * index 4: particle location
     */
    private List<Location> shopLocation = new ArrayList<>();

    /**
     * Constructor
     * @param locations
     *                  the incoming location array.
     */
    public ShopLocation(Location... locations) {
        for (int i = 0; i < locations.length; i++) {
            shopLocation.add(i, locations[i]);
        }
    }

    /**
     * Returns the shop location
     * @return
     *         a {@code List} associated with the locations related to where the shop is at.
     */
    public List<Location> getShopLocation() {
        return this.shopLocation;
    }

    /**
     * Overwrites the current shop location.
     * @param modification
     *                    the modified {@code List} that will overwrite {@code this}
     */
    public void modifyLocation(List<Location> modification) {
        this.shopLocation = modification;
    }

}
