package org.example.utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * 2-way linked list implementation that support random removal
 * in constant time. An extra Map is used to map from values to
 * the corresponding nodes in the list. In order to remove a value
 * from the list, first the node is retrieve from the map, then
 * the removal process will be executed on the retrieved node.
 */
public class DoublyLinkedList<V> {
    private int size;

    private Node<V> head;

    private Node<V> tail;

    private Map<V, Node<V>> valToNodeMap;

    public DoublyLinkedList() {
        this.size = 0;
        this.valToNodeMap = new HashMap<>();
    }

    public void add(V val) {
        if (valToNodeMap.containsKey(val)) return;

        Node<V> newNode = new Node<>(val);
        if (head == null) {
            head = newNode;
        } else {
            newNode.prev = tail;
            tail.next = newNode;
        }

        tail = newNode;
        valToNodeMap.put(val, newNode);
        size += 1;
    }

    public boolean remove(V val) {
        if (!valToNodeMap.containsKey(val)) return false;

        Node<V> node = valToNodeMap.get(val);
        removeNode(node);

        return true;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public Iterator<V> iterator() {
        return new Iterator<>(head);
    }

    private void removeNode(Node<V> node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        }

        if (node == head) {
            head = node.next;
        }
        if (node == tail) {
            tail = node.prev;
        }

        size -= 1;
    }

    private static class Node<V> {
        private V value;
        private Node<V> next;
        private Node<V> prev;

        public Node(V val) {
            this.value = val;
        }
    }

    /**
     * This iterator is only for iterating through the list
     * Only deleting current element is supported while iterating
     * Other updating operation might cause unexpected errors
     */
    public static class Iterator<V> {
        private Node<V> next;

        private Iterator(Node<V> head) {
            this.next = head;
        }

        public boolean hasNext() {
            return this.next != null;
        }

        public V next() {
            if (next == null) throw new IndexOutOfBoundsException();

            V val = next.value;
            next = next.next;
            return val;
        }
    }
}
