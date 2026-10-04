package kz.aitu.daa;

import kz.aitu.daa.metrics.Metrics;
import kz.aitu.daa.structures.MyLinkedList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MyLinkedListTest {

    private static MyLinkedList filled(int n) {
        MyLinkedList list = new MyLinkedList();
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        list.metrics().reset();
        return list;
    }

    private static void assertContent(MyLinkedList list, int... expected) {
        assertEquals(expected.length, list.size(), "size");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], list.get(i), "element at index " + i);
        }
    }

    @Test
    void appendCountsLinkUpdates() {
        MyLinkedList list = new MyLinkedList();
        list.add(1);
        assertEquals(2, list.metrics().getMoves());
        list.add(2);
        assertEquals(5, list.metrics().getMoves());
    }

    @Test
    void insertAtHeadIsConstantForAnySize() {
        for (int n : new int[] { 10, 1_000, 100_000 }) {
            MyLinkedList list = filled(n);
            list.add(0, -1);
            Metrics m = list.metrics();
            assertEquals(0, m.getSteps(), "steps for n=" + n);
            assertEquals(4, m.getMoves(), "moves for n=" + n);
        }
    }

    @Test
    void removeAtHeadIsConstantForAnySize() {
        for (int n : new int[] { 10, 1_000, 100_000 }) {
            MyLinkedList list = filled(n);
            assertEquals(0, list.remove(0));
            Metrics m = list.metrics();
            assertEquals(1, m.getSteps(), "steps for n=" + n);
            assertEquals(2, m.getMoves(), "moves for n=" + n);
        }
    }

    @Test
    void getTraversesFromNearestEnd() {
        MyLinkedList list = filled(100);
        list.get(0);
        assertEquals(1, list.metrics().getSteps());
        list.metrics().reset();
        list.get(99);
        assertEquals(1, list.metrics().getSteps());
        list.metrics().reset();
        list.get(49);
        assertEquals(50, list.metrics().getSteps());
        list.metrics().reset();
        list.get(50);
        assertEquals(50, list.metrics().getSteps());
    }

    @Test
    void insertInMiddleTraversesThenRelinks() {
        MyLinkedList list = filled(100);
        list.add(50, -1);
        assertEquals(49, list.metrics().getSteps());
        assertEquals(4, list.metrics().getMoves());
        assertEquals(-1, list.get(50));
        assertEquals(50, list.get(51));
    }

    @Test
    void containsOfMissingValueVisitsEveryNode() {
        MyLinkedList list = filled(100);
        assertFalse(list.contains(-5));
        assertEquals(100, list.metrics().getSteps());
        assertEquals(100, list.metrics().getComparisons());
    }

    @Test
    void removingTailUpdatesTail() {
        MyLinkedList list = new MyLinkedList();
        list.add(1);
        list.add(2);
        assertEquals(2, list.remove(1));
        list.add(3);
        assertContent(list, 1, 3);
    }

    @Test
    void removingOnlyElementClearsHeadAndTail() {
        MyLinkedList list = new MyLinkedList();
        list.add(1);
        list.remove(0);
        list.add(2);
        list.add(3);
        list.add(0, 1);
        assertContent(list, 1, 2, 3);
    }

    @Test
    void backwardLinksStayConsistentAfterHeadInserts() {
        MyLinkedList list = new MyLinkedList();
        for (int i = 0; i < 10; i++) {
            list.add(0, i);
        }
        assertContent(list, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);
        list.remove(9);
        list.remove(5);
        assertContent(list, 9, 8, 7, 6, 5, 3, 2, 1);
    }
}