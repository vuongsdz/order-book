package org.example.index;

import org.example.entities.Order;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OrderIndex {
    private static final Set<Order> EMPTY_ORDER_SET = Set.of();

    private final Map<Integer, Set<Order>> customerIdToOrdersMap;

    public OrderIndex() {
        this.customerIdToOrdersMap = new ConcurrentHashMap<>();
    }

    public void add(Order order) {
        customerIdToOrdersMap.computeIfAbsent(order.getCustomerId(), k -> new HashSet<>())
                .add(order);
    }

    public void remove(Order order) {
        customerIdToOrdersMap.computeIfPresent(
                order.getCustomerId(),
                (k, v) -> {
                    v.remove(order);
                    return v;
                });
    }

    public Collection<Order> findRestingOrdersByCustomer(int customerId) {
        return customerIdToOrdersMap.getOrDefault(customerId, EMPTY_ORDER_SET);
    }
}
