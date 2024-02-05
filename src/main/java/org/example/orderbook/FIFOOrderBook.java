package org.example.orderbook;

import org.example.dispatchers.MatchingResultDispatcher;
import org.example.dispatchers.NewRestingOrderEventDispatcher;
import org.example.dispatchers.OrderCancelledEventDispatcher;
import org.example.entities.BuyOrder;
import org.example.entities.MatchingResult;
import org.example.entities.Order;
import org.example.entities.SellOrder;
import org.example.order.OrderFactory;

/**
 * An OrderBook implementation using price/time priority algorithm
 * It maintains a {@link RestingSellQueue} to store pending sell order
 * and a {@link RestingBuyQueue} to store pending buy queue.
 *
 * <p>A buy order will first be matched with orders inside the sell queue.
 * If not matching order is found, the buy order will be added to buy queue
 * for future matching. Same for sell orders.
 *
 * <p>Note that this implementation is not synchronized. If multiple thread
 * want to access and modifying the orderBook concurrently, it must be
 * synchronized externally
 */
public class FIFOOrderBook implements OrderBook {
    private final RestingSellQueue restingSellQueue;

    private final RestingBuyQueue restingBuyQueue;

    public FIFOOrderBook() {
        this.restingBuyQueue = new RestingBuyQueue();
        this.restingSellQueue = new RestingSellQueue();
    }

    @Override
    public void buy(int customerId, int bookId, int price, Long expireAfterSeconds) {
        BuyOrder buyOrder = OrderFactory.buildBuyOrder(customerId, bookId, price, expireAfterSeconds);

        // find a matching sell order from pending sell order queue
        SellOrder sellOrder = restingSellQueue.match(buyOrder);
        if (sellOrder != null) {
            // if a matching order is founded, remove the matched order from pending queue
            // and dispatch the matching result
            restingSellQueue.remove(sellOrder);
            dispatchResult(sellOrder, buyOrder);
        } else {
            // if we cannot match the buy request with any pending sell order
            // add buy order to buy order queue
            restingBuyQueue.add(buyOrder);
            NewRestingOrderEventDispatcher.getInstance().dispatch(buyOrder);
        }
    }

    @Override
    public void sell(int customerId, int bookId, int price, Long expireAfterSeconds) {
        SellOrder sellOrder = OrderFactory.buildSellOrder(customerId, bookId, price, expireAfterSeconds);

        // find a matching buy order from pending buy order queue
        BuyOrder buyOrder = restingBuyQueue.match(sellOrder);
        if (buyOrder != null) {
            // if a matching order is founded, remove the matched order from pending queue
            // and dispatch the matching result
            restingBuyQueue.remove(buyOrder);
            dispatchResult(sellOrder, buyOrder);
        } else {
            // if we cannot match the sell request with any pending buy order
            // add sell order to sell order queue
            restingSellQueue.add(sellOrder);
            NewRestingOrderEventDispatcher.getInstance().dispatch(sellOrder);
        }
    }

    @Override
    public void cancel(Order order) {
        // to cancel and order, we just simply remove the order from the corresponding queue
        boolean isCancelled = false;
        if (order instanceof BuyOrder) {
            isCancelled = restingBuyQueue.remove((BuyOrder) order);
        } else if (order instanceof SellOrder) {
            isCancelled = restingSellQueue.remove((SellOrder) order);
        }

        OrderCancelledEventDispatcher.getInstance().dispatch(order, isCancelled);
    }

    private void dispatchResult(SellOrder sellOrder, BuyOrder buyOrder) {
        MatchingResult result = MatchingResult.builder()
                .buyOrder(buyOrder)
                .sellOrder(sellOrder)
                .build();
        MatchingResultDispatcher.getInstance().dispatch(result);
    }
}
