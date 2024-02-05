package org.example.orderbook;

import org.example.entities.Order;

/**
 * A wrapper of {@link FIFOOrderBook} that support parallel matching
 * to take advantage of multi-core processors.
 * Since we only need to maintain the order of buy/sell requests
 * of a same book, we can run the matching process in multiple thread
 * as long as we can guaranty that request of a same book go to a same thread.
 *
 * <p>{@link PartitionedOrderBook} achieved that by maintaining a list of
 * {@link QueueBasedOrderBook} instances, each run on its own thread.
 * Each {@link QueueBasedOrderBook} instance is considered a partition.
 * To determine which partition a request belong to, the bookId
 * is hashed by taking modulus with number of partitions.
 * hashValue = bookId % number_of_partitions
 * Then the hashValue is used as index in the partition list to get
 * the partition.
 * partition = partitionList.get(hashValue)
 */
public class PartitionedOrderBook implements OrderBook {
    /**
     * Partition list
     */
    private final OrderBook[] partitions;

    public PartitionedOrderBook(int size) {
        this.partitions = new OrderBook[size];
        for (int i = 0; i < size; i++) {
            this.partitions[i] = new QueueBasedOrderBook();
        }
    }

    @Override
    public void buy(int customerId, int bookId, int price, Long expireAfterSeconds) {
        getPartition(bookId).buy(customerId, bookId, price, expireAfterSeconds);
    }

    @Override
    public void sell(int customerId, int bookId, int price, Long expireAfterSeconds) {
        getPartition(bookId).sell(customerId, bookId, price, expireAfterSeconds);
    }

    @Override
    public void cancel(Order order) {
        getPartition(order.getBookId()).cancel(order);
    }

    private OrderBook getPartition(int bookId) {
        int partitionId = hashSlot(bookId);
        return partitions[partitionId];
    }

    private int hashSlot(int bookId) {
        return bookId % partitions.length;
    }
}
