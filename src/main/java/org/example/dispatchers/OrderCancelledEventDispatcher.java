package org.example.dispatchers;

import org.example.entities.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderCancelledEventDispatcher {
    private static final OrderCancelledEventDispatcher INSTANCE = new OrderCancelledEventDispatcher();

    public static OrderCancelledEventDispatcher getInstance() {
        return INSTANCE;
    }

    private final List<OrderCancelListener> listeners;

    private OrderCancelledEventDispatcher() {
        listeners = new ArrayList<>();
    }

    public void registerListener(OrderCancelListener listener) {
        listeners.add(listener);
    }

    public void dispatch(Order order, boolean isCancelled) {
        for (OrderCancelListener listener : listeners) {
            listener.onCancelled(order, isCancelled);
        }
    }
}
