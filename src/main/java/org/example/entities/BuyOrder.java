package org.example.entities;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BuyOrder extends AbstractOrder {
    @Override
    public OrderType getType() {
        return OrderType.BUY;
    }
}
