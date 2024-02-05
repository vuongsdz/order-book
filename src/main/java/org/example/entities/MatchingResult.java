package org.example.entities;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MatchingResult {
    private Order buyOrder;
    private Order sellOrder;
}
