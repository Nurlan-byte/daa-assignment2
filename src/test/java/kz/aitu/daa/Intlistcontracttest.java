package kz.aitu.daa;

import kz.aitu.daa.structures.DynamicArray;
import kz.aitu.daa.structures.IntList;
import kz.aitu.daa.structures.MyLinkedList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class IntListContractTest {

    static Stream<Arguments> lists() {
        return Stream.of(
                arguments(named("DynamicArray", (Supplier<IntList>) DynamicArray::new)),
                arguments(named("MyLinkedList", (Supplier<IntList>) MyLinkedList::new))
        );
    }

    private static IntList listOf(Supplier<IntList> factory, int... values) {
        IntList list = factory.get();
        for (int v : values) {
            list.add(v);
        }
        return list;
    }

    private static void assertContent(IntList list, int... expected) {
        assertEquals(expected.length, list.size(), "size");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], list.get(i), "element at index " + i);
        }
    }

    @ParameterizedTest
    @MethodSource("lists")
    void newListIsEmpty(Supplier<IntList> factory) {
        IntList list = factory.get();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertFalse(list.contains(0));
    }

    @ParameterizedTest
    @MethodSource("lists")
    void appendKeepsOrder(Supplier<IntList> factory) {
        IntList list = listOf(factory, 5, -3, 0, 42, 7);
        assertContent(list, 5, -3, 0, 42, 7);
        assertFalse(list.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("lists")
    void insertAtHeadMiddleAndTail(Supplier<IntList> factory) {
        IntList list = listOf(factory, 1, 2, 3);
        list.add(0, 10);
        assertContent(list, 10, 1, 2, 3);
        list.add(2, 20);
        assertContent(list, 10, 1, 20, 2, 3);
        list.add(list.size(), 30);
        assertContent(list, 10, 1, 20, 2, 3, 30);
    }

    @ParameterizedTest
    @MethodSource("lists")
    void insertIntoEmptyList(Supplier<IntList> factory) {
        IntList list = factory.get();
        list.add(0, 99);
        assertContent(list, 99);
    }

    @ParameterizedTest
    @MethodSource("lists")
    void removeFromHeadMiddleAndTail(Supplier<IntList> factory) {
        IntList list = listOf(factory, 10, 20, 30, 40, 50);
        assertEquals(10, list.remove(0));
        assertContent(list, 20, 30, 40, 50);
        assertEquals(30, list.remove(1));
        assertContent(list, 20, 40, 50);
        assertEquals(50, list.remove(list.size() - 1));
        assertContent(list, 20, 40);
    }

    @ParameterizedTest
    @MethodSource("lists")
    void singleElementAddRemoveAndReuse(Supplier<IntList> factory) {
        IntList list = listOf(factory, 7);
        assertEquals(7, list.remove(0));
        assertTrue(list.isEmpty());
        assertFalse(list.contains(7));
        list.add(8);
        assertContent(list, 8);
    }

    @ParameterizedTest
    @MethodSource("lists")
    void containsFindsPresentAndMissing(Supplier<IntList> factory) {
        IntList list = listOf(factory, 4, 8, 15, 16, 23, 42);
        assertTrue(list.contains(4));
        assertTrue(list.contains(16));
        assertTrue(list.contains(42));
        assertFalse(list.contains(5));
        assertFalse(list.contains(-42));
    }

    @ParameterizedTest
    @MethodSource("lists")
    void duplicatesAreKeptSeparately(Supplier<IntList> factory) {
        IntList list = listOf(factory, 5, 5, 5);
        assertContent(list, 5, 5, 5);
        list.remove(1);
        assertContent(list, 5, 5);
        assertTrue(list.contains(5));
        list.remove(0);
        list.remove(0);
        assertFalse(list.contains(5));
    }

    @ParameterizedTest
    @MethodSource("lists")
    void invalidIndicesOnEmptyListThrow(Supplier<IntList> factory) {
        IntList list = factory.get();
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, 5));
    }

    @ParameterizedTest
    @MethodSource("lists")
    void invalidIndicesOnFilledListThrow(Supplier<IntList> factory) {
        IntList list = listOf(factory, 1, 2, 3);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(3));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(4, 9));
        assertContent(list, 1, 2, 3);
    }

    @ParameterizedTest
    @MethodSource("lists")
    void holdsManyElements(Supplier<IntList> factory) {
        IntList list = factory.get();
        for (int i = 0; i < 10_000; i++) {
            list.add(i * 2);
        }
        assertEquals(10_000, list.size());
        for (int i = 0; i < 10_000; i++) {
            assertEquals(i * 2, list.get(i));
        }
    }

    @ParameterizedTest
    @MethodSource("lists")
    void matchesJavaArrayListOnRandomOperations(Supplier<IntList> factory) {
        IntList list = factory.get();
        List<Integer> expected = new ArrayList<>();
        Random random = new Random(42);

        for (int op = 0; op < 10_000; op++) {
            int value = random.nextInt(100);
            switch (random.nextInt(4)) {
                case 0 -> {
                    list.add(value);
                    expected.add(value);
                }
                case 1 -> {
                    int index = random.nextInt(expected.size() + 1);
                    list.add(index, value);
                    expected.add(index, value);
                }
                case 2 -> {
                    if (!expected.isEmpty()) {
                        int index = random.nextInt(expected.size());
                        assertEquals(expected.remove(index).intValue(), list.remove(index), "remove at op " + op);
                    }
                }
                default -> assertEquals(expected.contains(value), list.contains(value), "contains at op " + op);
            }
            assertEquals(expected.size(), list.size(), "size at op " + op);
        }

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).intValue(), list.get(i), "final element " + i);
        }
    }
}