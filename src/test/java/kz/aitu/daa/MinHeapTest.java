package kz.aitu.daa;

import kz.aitu.daa.metrics.Metrics;
import kz.aitu.daa.structures.MinHeap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinHeapTest {

    private static MinHeap heapOf(int... values) {
        MinHeap heap = new MinHeap();
        for (int v : values) {
            heap.insert(v);
        }
        return heap;
    }

    private static MinHeap ascending(int n) {
        MinHeap heap = new MinHeap();
        for (int i = 0; i < n; i++) {
            heap.insert(i);
        }
        heap.metrics().reset();
        return heap;
    }

    @Test
    void newHeapIsEmpty() {
        MinHeap heap = new MinHeap();
        assertEquals(0, heap.size());
        assertTrue(heap.isEmpty());
        assertTrue(heap.isValidHeap());
    }

    @Test
    void emptyHeapOperationsThrow() {
        MinHeap heap = new MinHeap();
        assertThrows(IllegalStateException.class, heap::peekMin);
        assertThrows(IllegalStateException.class, heap::extractMin);
    }

    @Test
    void singleElementInsertExtractAndReuse() {
        MinHeap heap = heapOf(42);
        assertEquals(42, heap.peekMin());
        assertEquals(42, heap.extractMin());
        assertTrue(heap.isEmpty());
        assertThrows(IllegalStateException.class, heap::extractMin);
        heap.insert(7);
        assertEquals(7, heap.peekMin());
        assertEquals(1, heap.size());
    }

    @Test
    void peekDoesNotRemove() {
        MinHeap heap = heapOf(3, 1, 2);
        assertEquals(1, heap.peekMin());
        assertEquals(1, heap.peekMin());
        assertEquals(3, heap.size());
    }

    @Test
    void duplicatesAndNegativesComeOutInOrder() {
        MinHeap heap = heapOf(5, -1, 5, -1, 3, 0);
        int[] expected = { -1, -1, 0, 3, 5, 5 };
        for (int e : expected) {
            assertEquals(e, heap.extractMin());
        }
        assertTrue(heap.isEmpty());
    }

    @Test
    void heapPropertyHoldsAfterEveryInsert() {
        Random random = new Random(42);
        MinHeap heap = new MinHeap();
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < 2_000; i++) {
            int v = random.nextInt(2_000_001) - 1_000_000;
            heap.insert(v);
            min = Math.min(min, v);
            assertTrue(heap.isValidHeap(), "heap property broken after insert " + i);
            assertEquals(min, heap.peekMin(), "wrong minimum after insert " + i);
        }
    }

    @Test
    void heapPropertyHoldsAfterEveryExtract() {
        Random random = new Random(7);
        MinHeap heap = new MinHeap();
        for (int i = 0; i < 2_000; i++) {
            heap.insert(random.nextInt(1_000));
        }
        while (!heap.isEmpty()) {
            heap.extractMin();
            assertTrue(heap.isValidHeap(), "heap property broken, size " + heap.size());
        }
    }

    @Test
    void extractAllGivesSortedOutput() {
        Random random = new Random(42);
        int n = 100_000;
        int[] data = new int[n];
        MinHeap heap = new MinHeap();
        for (int i = 0; i < n; i++) {
            data[i] = random.nextInt(2_000_001) - 1_000_000;
            heap.insert(data[i]);
        }
        Arrays.sort(data);
        for (int i = 0; i < n; i++) {
            assertEquals(data[i], heap.extractMin(), "element " + i);
        }
        assertTrue(heap.isEmpty());
    }

    @Test
    void descendingInputGivesSortedOutput() {
        MinHeap heap = new MinHeap();
        for (int i = 1_000; i > 0; i--) {
            heap.insert(i);
        }
        int prev = Integer.MIN_VALUE;
        while (!heap.isEmpty()) {
            int cur = heap.extractMin();
            assertTrue(prev <= cur, "order broken: " + prev + " then " + cur);
            prev = cur;
        }
    }

    @Test
    void matchesJavaPriorityQueueOnRandomOperations() {
        Random random = new Random(42);
        MinHeap heap = new MinHeap();
        PriorityQueue<Integer> expected = new PriorityQueue<>();
        for (int op = 0; op < 20_000; op++) {
            if (expected.isEmpty() || random.nextInt(3) > 0) {
                int v = random.nextInt(50);
                heap.insert(v);
                expected.add(v);
            } else {
                assertEquals(expected.poll().intValue(), heap.extractMin(), "extract at op " + op);
            }
            assertEquals(expected.size(), heap.size(), "size at op " + op);
            if (!expected.isEmpty()) {
                assertEquals(expected.peek().intValue(), heap.peekMin(), "peek at op " + op);
            }
        }
    }

    @Test
    void insertOfMaximumStaysLeaf() {
        MinHeap heap = ascending(1023);
        heap.insert(5_000);
        Metrics m = heap.metrics();
        assertEquals(1, m.getComparisons());
        assertEquals(0, m.getMoves());
    }

    @Test
    void insertOfNewMinimumClimbsWholeHeight() {
        MinHeap heap = ascending(1023);
        heap.insert(-1);
        Metrics m = heap.metrics();
        assertEquals(10, m.getComparisons());
        assertEquals(20, m.getMoves());
        assertEquals(-1, heap.peekMin());
    }

    @Test
    void extractMinIsLogarithmic() {
        MinHeap heap = ascending(1023);
        heap.extractMin();
        Metrics m = heap.metrics();
        assertTrue(m.getSteps() <= 10, "steps " + m.getSteps());
        assertTrue(m.getComparisons() <= 2 * 10, "comparisons " + m.getComparisons());
        assertTrue(m.getMoves() <= 1 + 2 * 10, "moves " + m.getMoves());
        assertTrue(heap.isValidHeap());
    }

    @Test
    void buildHeapProducesValidHeapAndSortedOutput() {
        Random random = new Random(42);
        int[] data = new int[10_000];
        for (int i = 0; i < data.length; i++) {
            data[i] = random.nextInt(2_000_001) - 1_000_000;
        }
        MinHeap heap = MinHeap.buildHeap(data);
        assertTrue(heap.isValidHeap());
        assertEquals(data.length, heap.size());
        int[] sorted = data.clone();
        Arrays.sort(sorted);
        for (int expected : sorted) {
            assertEquals(expected, heap.extractMin());
        }
    }

    @Test
    void buildHeapDoesNotChangeInput() {
        int[] data = { 5, 3, 8, 1 };
        MinHeap.buildHeap(data);
        assertArrayEquals(new int[] { 5, 3, 8, 1 }, data);
    }

    @Test
    void buildHeapOfEmptyArrayIsUsable() {
        MinHeap heap = MinHeap.buildHeap(new int[0]);
        assertTrue(heap.isEmpty());
        heap.insert(1);
        assertEquals(1, heap.peekMin());
    }

    @Test
    void builtHeapKeepsGrowingOnInsert() {
        MinHeap heap = MinHeap.buildHeap(new int[] { 3, 1, 2 });
        for (int i = 0; i < 100; i++) {
            heap.insert(100 - i);
        }
        assertTrue(heap.isValidHeap());
        assertEquals(103, heap.size());
        assertEquals(1, heap.extractMin());
    }

    @Test
    void buildHeapIsLinearWhileRepeatedInsertIsNot() {
        int n = 100_000;
        int[] descending = new int[n];
        for (int i = 0; i < n; i++) {
            descending[i] = n - i;
        }
        MinHeap built = MinHeap.buildHeap(descending);
        MinHeap inserted = new MinHeap();
        for (int v : descending) {
            inserted.insert(v);
        }
        long builtComparisons = built.metrics().getComparisons();
        long insertedComparisons = inserted.metrics().getComparisons();
        assertTrue(builtComparisons <= 2L * n, "buildHeap comparisons " + builtComparisons);
        assertTrue(insertedComparisons > 5 * builtComparisons,
                "insert " + insertedComparisons + " vs build " + builtComparisons);
    }

    @Test
    void extractMinWithEqualElementsStopsImmediately() {
        MinHeap heap = new MinHeap();
        for (int i = 0; i < 1023; i++) {
            heap.insert(5);
        }
        heap.metrics().reset();
        heap.extractMin();
        assertEquals(1, heap.metrics().getMoves());
        assertFalse(heap.metrics().getComparisons() > 2);
    }
}