package kz.aitu.daa.structures;

import kz.aitu.daa.metrics.Metrics;

public class MyLinkedList implements IntList {

    private static final class Node {
        int val;
        Node prev;
        Node next;

        Node(int val) {
            this.val = val;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private final Metrics metrics = new Metrics();

    @Override
    public void add(int x) {
        Node node = new Node(x);
        if (tail == null) {
            head = node;
            tail = node;
            metrics.moves(2);
        } else {
            node.prev = tail;
            tail.next = node;
            tail = node;
            metrics.moves(3);
        }
        size++;
    }

    @Override
    public void add(int index, int x) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int remove(int index) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        Node node = node(index);
        metrics.step();
        return node.val;
    }

    @Override
    public boolean contains(int x) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    private Node node(int index) {
        Node cur;
        if (index < size / 2) {
            cur = head;
            for (int i = 0; i < index; i++) {
                cur = cur.next;
                metrics.step();
            }
        } else {
            cur = tail;
            for (int i = size - 1; i > index; i--) {
                cur = cur.prev;
                metrics.step();
            }
        }
        return cur;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }
}