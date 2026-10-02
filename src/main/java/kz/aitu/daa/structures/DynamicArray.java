package kz.aitu.daa.structures;

import kz.aitu.daa.metrics.Metrics;

public class DynamicArray implements IntList {

    private static final int DEFAULT_CAPACITY = 8;

    private int[] data;
    private int size;
    private final Metrics metrics = new Metrics();

    public DynamicArray() {
        data = new int[DEFAULT_CAPACITY];
        size = 0;
    }

    @Override
    public void add(int x) {
        data[size] = x;
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
        metrics.step();
        return data[index];
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

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }
}