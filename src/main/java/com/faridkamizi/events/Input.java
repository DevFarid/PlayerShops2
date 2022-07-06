package com.faridkamizi.events;

public interface Input {

    public enum InputType {
        IntegerType, StringType, CUSTOM
    }

    public enum ShopEvent {
        SHOP_CREATION, OWNER_ADD_ITEM, OWNER_MODIFY_PRICE, SHOP_RENAME, PLAYER_BUY_EVENT
    }

}
