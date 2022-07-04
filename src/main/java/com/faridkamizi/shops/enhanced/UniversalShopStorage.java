package com.faridkamizi.shops.enhanced;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface UniversalShopStorage {
    final static Map<UUID, ShopObject> shopLocationDirectory = new HashMap<>();

    void add(UUID uuid, ShopObject shopObject);

}
