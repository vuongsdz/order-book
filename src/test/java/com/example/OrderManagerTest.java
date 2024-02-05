package com.example;

import lombok.SneakyThrows;
import org.example.OrderManager;
import org.example.dispatchers.MatchingResultDispatcher;
import org.example.orderbook.PartitionedOrderBook;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class OrderManagerTest {

    @Test
    public void givenABuyThenASellRequestWithASamePriceAndASameBook_then2OrdersAreMatched() {
        OrderManager orderManager = new OrderManager(new PartitionedOrderBook(2));
        int buyerId = 1;
        int sellerId = 2;
        int bookId = 1;
        int price = 10;

        AtomicBoolean success = new AtomicBoolean(false);

        MatchingResultDispatcher.getInstance().registerListener(result -> {
            assertEquals(result.getBuyOrder().getCustomerId(), buyerId);
            assertEquals(result.getSellOrder().getCustomerId(), sellerId);
            assertEquals(result.getSellOrder().getBookId(), bookId);
            assertEquals(result.getBuyOrder().getBookId(), bookId);
            assertEquals(result.getSellOrder().getExpectedPrice(), price);
            assertEquals(result.getBuyOrder().getExpectedPrice(), price);
            success.set(true);
        });

        orderManager.buy(1, 1, 10, null);
        orderManager.sell(2, 1, 10, null);

        try {
            Thread.sleep(1000);
        } catch (Exception ignore) {}

        assertTrue(success.get());
    }

    @Test
    public void givenASellThenABuyRequestWithASamePriceAndASameBook_then2OrdersAreMatched() {
        OrderManager orderManager = new OrderManager(new PartitionedOrderBook(2));
        int buyerId = 1;
        int sellerId = 2;
        int bookId = 1;
        int price = 10;

        AtomicBoolean success = new AtomicBoolean(false);

        MatchingResultDispatcher.getInstance().registerListener(result -> {
            assertEquals(result.getBuyOrder().getCustomerId(), buyerId);
            assertEquals(result.getSellOrder().getCustomerId(), sellerId);
            assertEquals(result.getSellOrder().getBookId(), bookId);
            assertEquals(result.getBuyOrder().getBookId(), bookId);
            assertEquals(result.getSellOrder().getExpectedPrice(), price);
            assertEquals(result.getBuyOrder().getExpectedPrice(), price);
            success.set(true);
        });

        orderManager.sell(sellerId, bookId, price, null);
        orderManager.buy(buyerId, bookId, price, null);

        try {
            Thread.sleep(1000);
        } catch (Exception ignore) {}

        assertTrue(success.get());
    }

    @Test
    public void givenMultipleSell_whenAMatchingBuyCome_thenMatchSuccessfullyWithLowestPrice() {
        OrderManager orderManager = new OrderManager(new PartitionedOrderBook(2));
        int buyerId = 1;
        int sellerId = 2;
        int bookId = 1;
        int buyPrice = 10;
        int sellPrice = 9;

        AtomicBoolean success = new AtomicBoolean(false);

        MatchingResultDispatcher.getInstance().registerListener(result -> {
            assertEquals(result.getBuyOrder().getCustomerId(), buyerId);
            assertEquals(result.getSellOrder().getCustomerId(), sellerId);
            assertEquals(result.getSellOrder().getBookId(), bookId);
            assertEquals(result.getBuyOrder().getBookId(), bookId);
            assertEquals(result.getSellOrder().getExpectedPrice(), sellPrice);
            assertEquals(result.getBuyOrder().getExpectedPrice(), buyPrice);
            success.set(true);
        });

        orderManager.sell(sellerId + 3, bookId, sellPrice + 1, null);
        orderManager.sell(sellerId + 1, bookId, sellPrice + 2, null);
        orderManager.sell(sellerId + 2, bookId, sellPrice + 5, null);
        orderManager.sell(sellerId, bookId, sellPrice, null);
        orderManager.sell(sellerId + 4, bookId, sellPrice + 8, null);
        orderManager.buy(buyerId, bookId, buyPrice, null);

        try {
            Thread.sleep(1000);
        } catch (Exception ignore) {}

        assertTrue(success.get());
    }

    @Test
    public void givenMultipleBuy_whenAMatchingSellCome_thenMatchSuccessfullyWithHighestPrice() {
        OrderManager orderManager = new OrderManager(new PartitionedOrderBook(2));
        int buyerId = 1;
        int sellerId = 10;
        int bookId = 1;
        int buyPrice = 10;
        int sellPrice = 9;

        AtomicBoolean success = new AtomicBoolean(false);

        MatchingResultDispatcher.getInstance().registerListener(result -> {
            assertEquals(result.getBuyOrder().getCustomerId(), buyerId);
            assertEquals(result.getSellOrder().getCustomerId(), sellerId);
            assertEquals(result.getSellOrder().getBookId(), bookId);
            assertEquals(result.getBuyOrder().getBookId(), bookId);
            assertEquals(result.getSellOrder().getExpectedPrice(), sellPrice);
            assertEquals(result.getBuyOrder().getExpectedPrice(), buyPrice);
            success.set(true);
        });

        orderManager.buy(buyerId + 1, bookId, buyPrice - 1, null);
        orderManager.buy(buyerId + 2, bookId, buyPrice - 2, null);
        orderManager.buy(buyerId, bookId, buyPrice, null);
        orderManager.buy(buyerId + 3, bookId, buyPrice -  3, null);
        orderManager.buy(buyerId + 4, bookId, buyPrice - 4, null);
        orderManager.sell(sellerId, bookId, sellPrice, null);

        try {
            Thread.sleep(1000);
        } catch (Exception ignore) {}

        assertTrue(success.get());
    }

    @Test
    public void givenMultipleBuy_whenANotMatchingSellCome_thenNoMatchHappened() {
        OrderManager orderManager = new OrderManager(new PartitionedOrderBook(2));
        int buyerId = 1;
        int sellerId = 10;
        int bookId = 1;
        int buyPrice = 10;
        int sellPrice = 100;

        AtomicBoolean success = new AtomicBoolean(false);

        MatchingResultDispatcher.getInstance().registerListener(result -> {
            success.set(true);
        });

        orderManager.buy(buyerId, bookId, buyPrice, null);
        orderManager.buy(buyerId + 1, bookId, buyPrice - 1, null);
        orderManager.buy(buyerId + 2, bookId, buyPrice - 2, null);
        orderManager.buy(buyerId + 3, bookId, buyPrice -  3, null);
        orderManager.buy(buyerId + 4, bookId, buyPrice - 4, null);
        orderManager.sell(sellerId, bookId, sellPrice, null);

        try {
            Thread.sleep(1000);
        } catch (Exception ignore) {}

        assertFalse(success.get());
    }

    @Test
    @SneakyThrows
    public void givenExpiredBuy_thenActiveBuy_whenAMatchingSellCome_thenMatchSuccessfully() {
        OrderManager orderManager = new OrderManager(new PartitionedOrderBook(2));
        int expiredBuyerId = 1;
        int activeBuyerId = 2;
        int sellerId = 10;
        int bookId = 1;
        int buyPrice = 10;
        int sellPrice = 9;

        AtomicBoolean success = new AtomicBoolean(false);

        MatchingResultDispatcher.getInstance().registerListener(result -> {
            assertEquals(result.getBuyOrder().getCustomerId(), activeBuyerId);
            assertEquals(result.getSellOrder().getCustomerId(), sellerId);
            assertEquals(result.getSellOrder().getBookId(), bookId);
            assertEquals(result.getBuyOrder().getBookId(), bookId);
            assertEquals(result.getSellOrder().getExpectedPrice(), sellPrice);
            assertEquals(result.getBuyOrder().getExpectedPrice(), buyPrice);
            success.set(true);
        });

        orderManager.buy(expiredBuyerId, bookId, buyPrice, 1L);
        Thread.sleep(1500);

        orderManager.buy(activeBuyerId, bookId, buyPrice, null);
        orderManager.sell(sellerId, bookId, sellPrice, null);

        Thread.sleep(1000);

        assertTrue(success.get());
    }

    @Test
    @SneakyThrows
    public void givenExpiredSell_thenActiveSell_whenAMatchingBuyCome_thenMatchSuccessfully() {
        OrderManager orderManager = new OrderManager(new PartitionedOrderBook(2));
        int buyerId = 1;
        int expiredSellerId = 10;
        int activeSellerId = 11;
        int bookId = 1;
        int buyPrice = 10;
        int sellPrice = 9;

        AtomicBoolean success = new AtomicBoolean(false);

        MatchingResultDispatcher.getInstance().registerListener(result -> {
            assertEquals(result.getBuyOrder().getCustomerId(), buyerId);
            assertEquals(result.getSellOrder().getCustomerId(), activeSellerId);
            assertEquals(result.getSellOrder().getBookId(), bookId);
            assertEquals(result.getBuyOrder().getBookId(), bookId);
            assertEquals(result.getSellOrder().getExpectedPrice(), sellPrice);
            assertEquals(result.getBuyOrder().getExpectedPrice(), buyPrice);
            success.set(true);
        });

        orderManager.sell(expiredSellerId, bookId, sellPrice, 1L);
        Thread.sleep(1500);

        orderManager.sell(activeSellerId, bookId, sellPrice, 1L);
        orderManager.buy(buyerId, bookId, buyPrice, null);

        Thread.sleep(1000);

        assertTrue(success.get());
    }

    @Test
    @SneakyThrows
    public void given2Sell_thenFirstSellIsCancelled_whenAMatchingBuyCome_thenMatchSuccessfully() {
        OrderManager orderManager = new OrderManager(new PartitionedOrderBook(2));
        int buyerId = 1;
        int firstSellerId = 10;
        int secondSellerId = 11;
        int bookId = 1;
        int buyPrice = 10;
        int sellPrice = 9;

        AtomicBoolean success = new AtomicBoolean(false);

        MatchingResultDispatcher.getInstance().registerListener(result -> {
            assertEquals(result.getBuyOrder().getCustomerId(), buyerId);
            assertEquals(result.getSellOrder().getCustomerId(), secondSellerId);
            assertEquals(result.getSellOrder().getBookId(), bookId);
            assertEquals(result.getBuyOrder().getBookId(), bookId);
            assertEquals(result.getSellOrder().getExpectedPrice(), sellPrice);
            assertEquals(result.getBuyOrder().getExpectedPrice(), buyPrice);
            success.set(true);
        });

        orderManager.sell(firstSellerId, bookId, sellPrice, 1L);
        orderManager.sell(secondSellerId, bookId, sellPrice, 1L);

        Thread.sleep(1000);
        orderManager.cancel(orderManager.findUnMatchedOrdersByCustomer(firstSellerId).stream().findFirst().get());
        orderManager.buy(buyerId, bookId, buyPrice, null);

        Thread.sleep(1000);

        assertTrue(success.get());
    }

    @Test
    @SneakyThrows
    public void givenACustomer_whenCustomerPlaceMultipleOrder_thenGetCustomerOrders_returnCorrectOrderList() {
        OrderManager orderManager = new OrderManager(new PartitionedOrderBook(2));
        int customerId = 1;
        int bookId1 = 1;
        int bookId2 = 2;
        int bookId3 = 3;
        int bookId4 = 4;

        orderManager.buy(customerId, bookId1, 10, null);
        orderManager.buy(customerId, bookId2, 20, null);
        orderManager.sell(customerId, bookId3, 30, null);
        orderManager.sell(customerId, bookId4, 40, null);

        Thread.sleep(2000);

        var orderList = orderManager.findUnMatchedOrdersByCustomer(customerId);
        assertEquals(4, orderList.size());
    }

    @Test
    @SneakyThrows
    public void givenACustomer_whenPlaceBuyAndSellOfASameBook_thenNoMatchHappened() {
        OrderManager orderManager = new OrderManager(new PartitionedOrderBook(2));
        int customerId = 1;
        int bookId = 1;

        AtomicBoolean success = new AtomicBoolean(false);

        MatchingResultDispatcher.getInstance().registerListener(result -> {
            success.set(true);
        });

        orderManager.buy(customerId, bookId, 10, null);
        orderManager.buy(customerId, bookId, 10, null);

        Thread.sleep(1000);

        assertFalse(success.get());
    }
}
