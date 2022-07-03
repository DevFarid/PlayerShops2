package com.faridkamizi.shops.enhanced;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface PlayerShopStorage {
    final static Map<UUID, EnhancedShopObject> shopLocationDirectory = new HashMap<>();

    void add(UUID uuid, EnhancedShopObject shopObject);

}
