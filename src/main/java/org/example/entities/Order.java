package org.example.entities;

import java.util.UUID;

public interface Order {
    UUID getId();
    int getCustomerId();
    int getBookId();
    int getExpectedPrice();
    OrderType getType();
    boolean isExpired();
}
