package org.example.dispatchers;

import org.example.entities.Order;

public interface NewRestingOrderListener {
    void onNewRestingOrder(Order order);
}
