package dev.makeev.recommendation.model;

public enum RecommendationType {
    COLLABORATIVE_FILTERING("collaborative", "Based on similar users"),
    CONTENT_BASED("content", "Based on your preferences"),
    TRENDING("trending", "Popular right now"),
    CATEGORY_BASED("category", "Similar to your interests"),
    CROSS_SELL("cross_sell", "Frequently bought together"),
    UP_SELL("up_sell", "Premium version of your items");
    
    private final String code;
    private final String description;
    
    RecommendationType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getDescription() { return description; }
}
