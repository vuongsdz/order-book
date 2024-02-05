package org.example.entities;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SellOrder extends AbstractOrder {
    @Override
    public OrderType getType() {
        return OrderType.SELL;
    }
}
