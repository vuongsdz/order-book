package org.example;

import org.example.dispatchers.MatchingResultDispatcher;
import org.example.dispatchers.NewRestingOrderEventDispatcher;
import org.example.dispatchers.OrderCancelledEventDispatcher;
import org.example.entities.MatchingResult;
import org.example.entities.Order;
import org.example.index.OrderIndex;
import org.example.orderbook.OrderBook;

import java.util.Collection;

/**
 * OrderManager is a wrapper class that provide interface to buy,
 * sell books, cancel pending orders.
 */
public class OrderManager {
    /**
     * OrderBook provide functionality for buy, sell books, cancel orders
     */
    private final OrderBook orderBook;

    /**
     * OrderIndex index orders by customer_id and is used for fast retrieving
     * active orders of a customer
     */
    private final OrderIndex orderIndex;

    public OrderManager(OrderBook orderBook) {
        this.orderBook = orderBook;
        this.orderIndex = new OrderIndex();

        // Register a listener that remove matched orders from orderIndex
        // whenever 2 orders are matched
        MatchingResultDispatcher.getInstance().registerListener(result -> {
            orderIndex.remove(result.getBuyOrder());
            orderIndex.remove(result.getSellOrder());
        });

        // Register a listener that remove orders from orderIndex whenever they are cancelled
        OrderCancelledEventDispatcher.getInstance().registerListener((order, success) -> {
            if (success) {
                orderIndex.remove(order);
            }
        });
        NewRestingOrderEventDispatcher.getInstance().registerListener(orderIndex::add);

        // Register matching result handler
        // For the sake of simplicity, we directly log the matching result to the console
        // In real live situation, we probably need to push to results into a queue
        // and have difference threads polling from the queue to process the results
        MatchingResultDispatcher.getInstance().registerListener(this::logMatchingResult);
    }

    public void buy(int customerId, int bookId, int price, Long expireAfterSeconds) {
        orderBook.buy(customerId, bookId, price, expireAfterSeconds);
    }

    public void sell(int customerId, int bookId, int price, Long expireAfterSeconds) {
        orderBook.sell(customerId, bookId, price, expireAfterSeconds);
    }

    /**
     * Cancel an order. Return true if order is waiting to be matched and has been
     * cancelled successfully, otherwise return false.
     */
    public void cancel(Order order) {
        orderBook.cancel(order);
    }

    public Collection<Order> findUnMatchedOrdersByCustomer(int customerId) {
        return orderIndex.findRestingOrdersByCustomer(customerId);
    }

    private void logMatchingResult(MatchingResult result) {
        System.out.println(
                "Matched seller id " + result.getSellOrder().getCustomerId() +
                        " with buyer id " + result.getBuyOrder().getCustomerId() +
                        ". Book id: " + result.getSellOrder().getBookId() +
                        ". Price: " + result.getSellOrder().getExpectedPrice()
        );
    }
}
