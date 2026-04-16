package dev.makeev.common.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProductBindings {
    PRODUCT_EVENTS_OUTPUT("productEvents-out-0");

    private final String bindingName;
}
