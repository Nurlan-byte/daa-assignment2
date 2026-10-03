package kz.aitu.daa;

import kz.aitu.daa.metrics.Metrics;
import kz.aitu.daa.structures.DynamicArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicArrayTest {

    private static DynamicArray filled(int n) {
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < n; i++) {
            array.add(i);
        }
        array.metrics().reset();
        return array;
    }

    @Test
    void doublesCapacityWhenFull() {
        DynamicArray array = new DynamicArray();
        assertEquals(8, array.capacity());
        for (int i = 0; i < 8; i++) {
            array.add(i);
        }
        assertEquals(8, array.capacity());
        array.add(8);
        assertEquals(16, array.capacity());
        for (int i = 0; i <= 8; i++) {
            assertEquals(i, array.get(i));
        }
    }

    @Test
    void resizeCopiesEveryElementOnce() {
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < 9; i++) {
            array.add(i);
        }
        assertEquals(8, array.metrics().getMoves());
    }

    @Test
    void appendIsAmortizedConstant() {
        int n = 1024;
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < n; i++) {
            array.add(i);
        }
        assertTrue(array.metrics().getMoves() < 2L * n,
                "total copies must stay below 2n, was " + array.metrics().getMoves());
    }

    @Test
    void insertAtHeadShiftsAllElements() {
        DynamicArray array = filled(100);
        array.add(0, -1);
        assertEquals(100, array.metrics().getMoves());
        assertEquals(-1, array.get(0));
        assertEquals(99, array.get(100));
    }

    @Test
    void insertAtEndShiftsNothing() {
        DynamicArray array = filled(100);
        array.add(100, -1);
        assertEquals(0, array.metrics().getMoves());
    }

    @Test
    void removeAtHeadShiftsRemainingElements() {
        DynamicArray array = filled(100);
        assertEquals(0, array.remove(0));
        assertEquals(99, array.metrics().getMoves());
        assertEquals(1, array.get(0));
    }

    @Test
    void getCountsOneStep() {
        DynamicArray array = filled(100);
        array.get(50);
        Metrics m = array.metrics();
        assertEquals(1, m.getSteps());
        assertEquals(0, m.getMoves());
    }

    @Test
    void containsOfMissingValueScansWholeArray() {
        DynamicArray array = filled(100);
        array.contains(-5);
        assertEquals(100, array.metrics().getSteps());
        assertEquals(100, array.metrics().getComparisons());
    }

    @Test
    void containsStopsAtFirstMatch() {
        DynamicArray array = filled(100);
        array.contains(0);
        assertEquals(1, array.metrics().getComparisons());
    }
}