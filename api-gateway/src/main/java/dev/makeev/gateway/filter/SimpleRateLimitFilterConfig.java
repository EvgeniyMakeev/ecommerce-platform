package dev.makeev.gateway.filter;

import lombok.Data;

@Data
public class SimpleRateLimitFilterConfig {
    private String keyType = "ip";
    private int limit = 100;
    private int windowSeconds = 60;

}
