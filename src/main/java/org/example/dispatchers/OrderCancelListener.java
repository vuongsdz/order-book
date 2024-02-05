package org.example.dispatchers;

import org.example.entities.Order;

public interface OrderCancelListener {
    void onCancelled(Order order, boolean isCancelled);
}
