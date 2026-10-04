package kz.aitu.daa.structures;

import kz.aitu.daa.metrics.Metrics;

public class MinHeap {

    private static final int DEFAULT_CAPACITY = 8;

    private int[] heap;
    private int size;
    private final Metrics metrics = new Metrics();

    public MinHeap() {
        this(DEFAULT_CAPACITY);
    }

    private MinHeap(int capacity) {
        heap = new int[capacity];
        size = 0;
    }

    public static MinHeap buildHeap(int[] values) {
        MinHeap result = new MinHeap(Math.max(DEFAULT_CAPACITY, values.length));
        for (int i = 0; i < values.length; i++) {
            result.heap[i] = values[i];
        }
        result.size = values.length;
        for (int i = result.size / 2 - 1; i >= 0; i--) {
            result.siftDown(i);
        }
        return result;
    }

    public void insert(int x) {
        if (size == heap.length) {
            grow();
        }
        heap[size] = x;
        size++;
        siftUp(size - 1);
    }

    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        metrics.step();
        return heap[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        metrics.step();
        int min = heap[0];
        size--;
        if (size > 0) {
            heap[0] = heap[size];
            metrics.move();
            siftDown(0);
        }
        return min;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Metrics metrics() {
        return metrics;
    }

    public boolean isValidHeap() {
        for (int i = 1; i < size; i++) {
            if (heap[(i - 1) / 2] > heap[i]) {
                return false;
            }
        }
        return true;
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            metrics.step();
            metrics.compare();
            if (heap[parent] <= heap[i]) {
                break;
            }
            swap(i, parent);
            i = parent;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= size) {
                break;
            }
            int right = left + 1;
            int smallest = left;
            metrics.step();
            if (right < size) {
                metrics.compare();
                if (heap[right] < heap[left]) {
                    smallest = right;
                }
            }
            metrics.compare();
            if (heap[i] <= heap[smallest]) {
                break;
            }
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int a, int b) {
        int tmp = heap[a];
        heap[a] = heap[b];
        heap[b] = tmp;
        metrics.moves(2);
    }

    private void grow() {
        int[] newHeap = new int[heap.length * 2];
        for (int i = 0; i < size; i++) {
            newHeap[i] = heap[i];
            metrics.move();
        }
        heap = newHeap;
    }
}