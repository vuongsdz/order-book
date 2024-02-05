package org.example.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
public abstract class AbstractOrder implements Order {
    private final UUID id;

    private final int customerId;

    private final int bookId;

    private final int expectedPrice;

    @EqualsAndHashCode.Exclude
    private Long expiryTimeMillis;

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractOrder)) return false;

        return id.equals(((AbstractOrder) obj).getId());
    }

    @Override
    public boolean isExpired() {
        return expiryTimeMillis != null && expiryTimeMillis <= System.currentTimeMillis();
    }
}
