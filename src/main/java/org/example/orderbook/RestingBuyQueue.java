package org.example.orderbook;

import org.example.entities.BuyOrder;
import org.example.entities.SellOrder;
import org.example.utilities.DoublyLinkedList;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * {@link RestingBuyQueue} store pending buy orders and match a sell order
 * with the lowest price buy order.
 *
 * <p>In order to find a matching order, first we need to find the highest possible price
 * among pending orders. To do that, we maintain a TreeMap that map from sell prices to
 * corresponding orders. A {@link TreeMap} is a Map that maintain the order of its keys using
 * Red-Black tree, so it allow to do range searching, random insertion and removal in O(log(N)).
 *
 * <p>After the highest price is found, we get all the orders that offer the price from the order Map.
 * Then we iterate through the orders in ascending order of creation time to find the matching one.
 * We use {@link DoublyLinkedList} to store the orders because it supports random insert,
 * delete in O(1) time complexity. See {@link DoublyLinkedList} implementation for more detail.
 *
 * <p>Note that {@link TreeMap} is implemented using Red-Black tree, which provide faster insertion and
 * removal, but slower in search operations compare to AVL tree. Because read is more
 * frequent, we could consider using AVL tree instead.
 */
public class RestingBuyQueue {
    private final NavigableMap<Integer, DoublyLinkedList<BuyOrder>> priceToOrdersMap;

    public RestingBuyQueue() {
        priceToOrdersMap = new TreeMap<>();
    }

    public void add(BuyOrder buyOrder) {
        priceToOrdersMap.computeIfAbsent(buyOrder.getExpectedPrice(), price -> new DoublyLinkedList<>())
                .add(buyOrder);
    }

    /**
     * Find a matching buy order that match the given sell request
     * We need to iterate though all the possible prices and orders of those prices
     * Because earliest order is always at the head of the linkedList, in most case,
     * time complexity of matching operation is O(log(N)) where N is number of orders
     * that offer the lowest price.
     *
     * <p>Problem arise when there are expired orders or order from the same customer.
     * In that case, the order will be removed or ignored depend on specific case.
     * So in worst case, time complexity is O(M*log(N)^2) where M is number of offered prices,
     * N is number of orders of each price.
     */
    public BuyOrder match(SellOrder sellOrder) {
        if (priceToOrdersMap.isEmpty()) return null;

        int navigator;
        Integer highestPrice = priceToOrdersMap.lastKey();

        // Iterate through all possible price, from highest to lowest
        // util a matching order is found
        while (highestPrice != null && highestPrice >= sellOrder.getExpectedPrice()) {
            // Get all sell orders that offer the highestPrice
            DoublyLinkedList<BuyOrder> matchedOrderList = priceToOrdersMap.get(highestPrice);
            // Find the matching order inside the order list that we got above
            BuyOrder matchedOrder = findMatchedOrderInList(sellOrder, matchedOrderList);
            if (matchedOrder != null)
                return matchedOrder;

            // Matching order not found
            // Update the highestPrice to the greatest price that smaller than the current highestPrice
            navigator = highestPrice;
            highestPrice = priceToOrdersMap.lowerKey(navigator);
        }

        return null;
    }

    public boolean remove(BuyOrder order) {
        DoublyLinkedList<BuyOrder> orderList = priceToOrdersMap.get(order.getExpectedPrice());
        if (orderList != null && orderList.isNotEmpty()) {
            return orderList.remove(order);
        }

        return false;
    }

    /**
     * Match a sell order with a list of buy orders
     */
    private BuyOrder findMatchedOrderInList(SellOrder sellOrder, DoublyLinkedList<BuyOrder> buyOrders) {
        DoublyLinkedList.Iterator<BuyOrder> iterator = buyOrders.iterator();
        // Iterate through to buy orders
        // if current buy order have same customer id with sell order, ignore the order
        // if current buy order is expired, remove the order and move to the next one
        // otherwise, return the current buy order as it is the matching order
        while (iterator.hasNext()) {
            BuyOrder order = iterator.next();

            if (order.getCustomerId() != sellOrder.getCustomerId() && !order.isExpired()) {
                return order;
            }

            if (order.isExpired()) {
                buyOrders.remove(order);
            }
        }

        return null;
    }
}
