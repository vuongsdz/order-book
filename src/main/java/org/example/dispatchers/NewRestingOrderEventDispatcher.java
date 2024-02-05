package org.example.dispatchers;

import org.example.entities.Order;

import java.util.ArrayList;
import java.util.List;

public class NewRestingOrderEventDispatcher {
    private static final NewRestingOrderEventDispatcher INSTANCE = new NewRestingOrderEventDispatcher();

    public static NewRestingOrderEventDispatcher getInstance() {
        return INSTANCE;
    }

    private final List<NewRestingOrderListener> listeners;

    private NewRestingOrderEventDispatcher() {
        listeners = new ArrayList<>();
    }

    public void registerListener(NewRestingOrderListener listener) {
        listeners.add(listener);
    }

    public void dispatch(Order order) {
        for (NewRestingOrderListener listener : listeners) {
            listener.onNewRestingOrder(order);
        }
    }
}
