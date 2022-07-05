package com.faridkamizi.system;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface UniversalShopStorage {
    final static Map<UUID, ShopObject> shopLocationDirectory = new HashMap<>();

    void add(UUID uuid, ShopObject shopObject);

    public static ShopConfig get(UUID uuid, ShopObject shopObject) {
        if (shopLocationDirectory.containsKey(uuid))
            return null;

        return shopLocationDirectory.get(uuid).getShopConfig();
    }

    public static ShopObject get(UUID uuid) {
        if (shopLocationDirectory.containsKey(uuid))
            return null;

        return shopLocationDirectory.get(uuid);
    }
}
