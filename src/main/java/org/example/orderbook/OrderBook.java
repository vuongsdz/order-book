package org.example.orderbook;

import org.example.entities.Order;

/**
 * This interface provide functionality for buy, sell books, cancel pending orders
 */
public interface OrderBook {
    void buy(int customerId, int bookId, int price, Long expireAfterSeconds);

    void sell(int customerId, int bookId, int price, Long expireAfterSeconds);

    void cancel(Order order);
}
