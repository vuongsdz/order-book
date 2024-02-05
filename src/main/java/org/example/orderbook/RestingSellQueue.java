package org.example.orderbook;

import org.example.entities.BuyOrder;
import org.example.entities.SellOrder;
import org.example.utilities.DoublyLinkedList;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * {@link RestingSellQueue} store pending sell orders and match a buy order
 * with the lowest price sell order.
 *
 * <p>In order to find a matching order, first we need to find the lowest possible price
 * among pending orders. To do that, we maintain a TreeMap that map from sell prices to
 * corresponding orders. A {@link TreeMap} is a Map that maintain the order of its keys using
 * Red-Black tree, so it allow to do range searching, random insertion and removal in O(log(N)).
 *
 * <p>After the lowest price is found, we get all the orders that offer the price from the order Map.
 * Then we iterate through the orders in ascending order of creation time to find the matching one.
 * We use {@link DoublyLinkedList} to store the orders because it supports random insert,
 * delete in O(1) time complexity. See {@link DoublyLinkedList} implementation for more detail.
 *
 * <p>Note that {@link TreeMap} is implemented using Red-Black tree, which provide faster insertion and
 * removal, but slower in search operations compare to AVL tree. Because read is more
 * frequent, we could consider using AVL tree instead.
 */
public class RestingSellQueue {
    private final NavigableMap<Integer, DoublyLinkedList<SellOrder>> priceToOrdersMap;

    public RestingSellQueue() {
        priceToOrdersMap = new TreeMap<>();
    }

    public void add(SellOrder sellOrder) {
        priceToOrdersMap.computeIfAbsent(sellOrder.getExpectedPrice(), price -> new DoublyLinkedList<>())
                .add(sellOrder);
    }

    /**
     * Find a matching sell order that match the given buy request
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
    public SellOrder match(BuyOrder buyOrder) {
        if (priceToOrdersMap.isEmpty()) return null;

        Integer lowestPrice = priceToOrdersMap.firstKey();

        // Iterate through all possible price, from lowest to highest
        // util a matching order is found
        while (lowestPrice != null && lowestPrice <= buyOrder.getExpectedPrice()) {
            // Get all sell orders that offer the lowestPrice
            DoublyLinkedList<SellOrder> matchedOrderList = priceToOrdersMap.get(lowestPrice);
            // Find the matching order inside the order list that we got above
            SellOrder matchedOrder  = findMatchedOrderInList(buyOrder, matchedOrderList);
            if (matchedOrder != null)
                return matchedOrder;

            // Matching order not found
            // Update the lowestPrice to the smallest price that higher than the current lowestPrice
            lowestPrice = priceToOrdersMap.higherKey(lowestPrice);
        }

        return null;
    }

    public boolean remove(SellOrder order) {
        DoublyLinkedList<SellOrder> orderList = priceToOrdersMap.get(order.getExpectedPrice());
        if (orderList != null && orderList.isNotEmpty()) {
            return orderList.remove(order);
        }

        return false;
    }

    /**
     * Match a buy order with a list of sell orders
     */
    private SellOrder findMatchedOrderInList(BuyOrder buyOrder, DoublyLinkedList<SellOrder> sellOrders) {
        DoublyLinkedList.Iterator<SellOrder> iterator = sellOrders.iterator();
        // Iterate through to sell orders
        // if current sell order have same customer id with buy order, ignore the order
        // if current sell order is expired, remove the order and move to the next one
        // otherwise, return the current sell order as it is the matching order
        while (iterator.hasNext()) {
            SellOrder order = iterator.next();

            if (order.getCustomerId() != buyOrder.getCustomerId() && !order.isExpired()) {
                return order;
            }

            if (order.isExpired()) {
                sellOrders.remove(order);
            }
        }

        return null;
    }
}
