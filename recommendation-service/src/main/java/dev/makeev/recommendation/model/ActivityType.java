package dev.makeev.recommendation.model;

import lombok.Getter;

@Getter
public enum ActivityType {
    VIEW("view", "Product viewed"),
    SEARCH("search", "Product searched"),
    ADD_TO_CART("add_to_cart", "Product added to cart"),
    PURCHASE("purchase", "Product purchased"),
    WISHLIST("wishlist", "Product added to wishlist"),
    COMPARE("compare", "Product compared"),
    REVIEW("review", "Product reviewed"),
    SHARE("share", "Product shared");
    
    private final String code;
    private final String description;
    
    ActivityType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
}
