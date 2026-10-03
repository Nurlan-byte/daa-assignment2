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
        if (size == data.length) {
            grow();
        }
        data[size] = x;
        size++;
    }

    @Override
    public void add(int index, int x) {
        checkPositionIndex(index);
        if (size == data.length) {
            grow();
        }
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            metrics.move();
        }
        data[index] = x;
        size++;
    }

    @Override
    public int remove(int index) {
        checkIndex(index);
        metrics.step();
        int removed = data[index];
        for (int j = index; j < size - 1; j++) {
            data[j] = data[j + 1];
            metrics.move();
        }
        size--;
        return removed;
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        metrics.step();
        return data[index];
    }

    @Override
    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            metrics.step();
            metrics.compare();
            if (data[i] == x) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    public int capacity() {
        return data.length;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    private void grow() {
        int[] newData = new int[data.length * 2];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
            metrics.move();
        }
        data = newData;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }
}