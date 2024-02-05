package org.example.orderbook;

import lombok.Builder;
import lombok.Getter;
import org.example.entities.Order;

import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.onSpinWait;

/**
 * A wrapper of {@link FIFOOrderBook} that support calling buy/sell/cancel
 * operations from multiple threads.
 * {@link FIFOOrderBook} handle the synchronization internally using an event loop,
 * so we can have multiple thread handling incoming buy/sell requests if needed.
 *
 * <p>In this implementation, tha actual matching process is still running in a single thread.
 * Only part that can run in parallel is the order producing process.
 */
public class QueueBasedOrderBook implements OrderBook {
    /**
     * Request queue that the event loop will read from
     */
    private final ConcurrentLinkedQueue<Request> requestQueue;

    /**
     * OrderBook instance that handle the actual matching logic
     */
    private final OrderBook orderBook;

    public QueueBasedOrderBook() {
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.orderBook = new FIFOOrderBook();

        // Create an event loop that constantly polling
        // new matching request from the request queue
        new Thread(() -> {
            while (true) {
                if (requestQueue.isEmpty()) {
                    onSpinWait();
                }
                processRequest(requestQueue.poll());
            }
        }).start();
    }

    @Override
    public void buy(int customerId, int bookId, int price, Long expireAfterSeconds) {
        Request request = Request.builder()
                .customerId(customerId)
                .bookId(bookId)
                .price(price)
                .expireAfterSeconds(expireAfterSeconds)
                .type(RequestType.BUY)
                .build();
        requestQueue.offer(request);
    }

    @Override
    public void sell(int customerId, int bookId, int price, Long expireAfterSeconds) {
        Request request = Request.builder()
                .customerId(customerId)
                .bookId(bookId)
                .price(price)
                .expireAfterSeconds(expireAfterSeconds)
                .type(RequestType.SELL)
                .build();
        requestQueue.offer(request);
    }

    @Override
    public void cancel(Order order) {
        Request request = Request.builder()
                .order(order)
                .type(RequestType.CANCEL)
                .build();
        requestQueue.offer(request);
    }

    private void processRequest(Request request) {
        if (request == null) return;

        switch (request.getType()) {
            case BUY:
                orderBook.buy(
                        request.getCustomerId(), request.getBookId(), request.getPrice(),
                        request.getExpireAfterSeconds()
                );
                break;
            case SELL:
                orderBook.sell(
                        request.getCustomerId(), request.getBookId(), request.getPrice(),
                        request.getExpireAfterSeconds()
                );
                break;
            case CANCEL:
                orderBook.cancel(request.getOrder());
                break;
        }
    }

    @Getter
    @Builder
    private static class Request {
        private int customerId;
        private int bookId;
        private int price;
        private Long expireAfterSeconds;
        private Order order;
        private RequestType type;
    }

    private enum RequestType {
        BUY, SELL, CANCEL
    }
}
