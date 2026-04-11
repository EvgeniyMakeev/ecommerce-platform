package dev.makeev.gateway.filter;

public class SimpleRateLimitFilterConfig {
    private String keyType = "ip";
    private int limit = 100;
    private int windowSeconds = 60;

    public String getKeyType() { 
        return keyType; 
    }
    
    public void setKeyType(String keyType) { 
        this.keyType = keyType; 
    }

    public int getLimit() { 
        return limit; 
    }
    
    public void setLimit(int limit) { 
        this.limit = limit; 
    }

    public int getWindowSeconds() { 
        return windowSeconds; 
    }
    
    public void setWindowSeconds(int windowSeconds) { 
        this.windowSeconds = windowSeconds; 
    }
}
