package org.example.order;

import org.example.entities.BuyOrder;
import org.example.entities.SellOrder;

import java.util.UUID;

public class OrderFactory {
    public static SellOrder buildSellOrder(int customerId, int bookId, int price, Long expireAfterSeconds) {
        return SellOrder.builder()
                .id(UUID.randomUUID())
                .bookId(bookId)
                .customerId(customerId)
                .expectedPrice(price)
                .expiryTimeMillis(expiryTime(expireAfterSeconds))
                .build();
    }

    public static BuyOrder buildBuyOrder(int customerId, int bookId, int price, Long expireAfterSeconds) {
        return BuyOrder.builder()
                .id(UUID.randomUUID())
                .bookId(bookId)
                .customerId(customerId)
                .expectedPrice(price)
                .expiryTimeMillis(expiryTime(expireAfterSeconds))
                .build();
    }

    private static Long expiryTime(Long expireAfterSeconds) {
        return expireAfterSeconds != null ? System.currentTimeMillis() + expireAfterSeconds * 1000 : null;
    }
}
