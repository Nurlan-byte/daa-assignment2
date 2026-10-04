package kz.aitu.daa.bench;

import kz.aitu.daa.metrics.CsvWriter;
import kz.aitu.daa.metrics.Metrics;
import kz.aitu.daa.structures.DynamicArray;
import kz.aitu.daa.structures.IntList;
import kz.aitu.daa.structures.MinHeap;
import kz.aitu.daa.structures.MyLinkedList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

public final class Benchmark {

    private static final int[] SIZES = { 100, 1_000, 10_000, 100_000 };
    private static final long SEED = 42L;
    private static final int VALUE_BOUND = 1_000_000;

    private static final int GLOBAL_WARMUP_ROUNDS = 3;
    private static final int GLOBAL_WARMUP_SIZE = 10_000;
    private static final int WARMUP_RUNS = 5;
    private static final int MEASURED_RUNS = 5;

    private static final int W1_QUERIES = 10_000;
    private static final int W2_QUERIES = 1_000;
    private static final int W3_OPERATIONS = 1_000;

    private static final Path RESULTS = Path.of("results", "results.csv");
    private static final Path BUILD_HEAP_RESULTS = Path.of("results", "buildheap.csv");

    private static final List<ListKind> LIST_KINDS = List.of(
            new ListKind("DynamicArray", DynamicArray::new),
            new ListKind("MyLinkedList", MyLinkedList::new));

    private static long blackhole;

    private record ListKind(String name, Supplier<IntList> factory) {
    }

    private record Measurement(double timeMs, Metrics metrics) {
    }

    private record Data(int n, int[] values, int[] randomIndices, int[] searchQueries, int[] insertValues) {

        static Data generate(int n) {
            Random random = new Random(SEED);

            int[] values = new int[n];
            for (int i = 0; i < n; i++) {
                values[i] = random.nextInt(VALUE_BOUND);
            }

            int[] indices = new int[W1_QUERIES];
            for (int i = 0; i < W1_QUERIES; i++) {
                indices[i] = random.nextInt(n);
            }

            int[] queries = new int[W2_QUERIES];
            int half = W2_QUERIES / 2;
            for (int i = 0; i < half; i++) {
                queries[i] = values[random.nextInt(n)];
            }
            for (int i = half; i < W2_QUERIES; i++) {
                queries[i] = -1 - random.nextInt(VALUE_BOUND);
            }
            shuffle(queries, random);

            int[] insertValues = new int[W3_OPERATIONS];
            for (int i = 0; i < W3_OPERATIONS; i++) {
                insertValues[i] = random.nextInt(VALUE_BOUND);
            }

            return new Data(n, values, indices, queries, insertValues);
        }

        private static void shuffle(int[] a, Random random) {
            for (int i = a.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
            }
        }
    }

    private Benchmark() {
    }

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();
        System.out.println("JIT warm-up...");
        Data warmupData = Data.generate(GLOBAL_WARMUP_SIZE);
        for (int round = 0; round < GLOBAL_WARMUP_ROUNDS; round++) {
            runAll(null, null, warmupData);
        }
        try (CsvWriter csv = new CsvWriter(RESULTS);
                CsvWriter bonus = new CsvWriter(BUILD_HEAP_RESULTS)) {
            for (int n : SIZES) {
                runAll(csv, bonus, Data.generate(n));
            }
        }
        System.out.printf(Locale.US, "%nDone in %.1f s. Results: %s, %s (checksum %d)%n",
                (System.nanoTime() - start) / 1e9, RESULTS, BUILD_HEAP_RESULTS, blackhole);
    }

    private static void runAll(CsvWriter csv, CsvWriter bonus, Data d) throws IOException {
        runListWorkloads(csv, d);
        runPriorityWorkload(csv, d);
        runBuildHeapComparison(bonus, d);
    }

    private static void runListWorkloads(CsvWriter csv, Data d) throws IOException {
        for (ListKind kind : LIST_KINDS) {
            record(csv, "W1", "-", kind.name(), d.n(), measure(() -> randomAccess(kind.factory(), d)));
        }
        for (ListKind kind : LIST_KINDS) {
            record(csv, "W2", "-", kind.name(), d.n(), measure(() -> search(kind.factory(), d)));
        }
        for (ListKind kind : LIST_KINDS) {
            record(csv, "W3", "head", kind.name(), d.n(),
                    measure(() -> insertRemove(kind.factory(), d, 0)));
            record(csv, "W3", "middle", kind.name(), d.n(),
                    measure(() -> insertRemove(kind.factory(), d, d.n() / 2)));
        }
    }

    private static void runPriorityWorkload(CsvWriter csv, Data d) throws IOException {
        record(csv, "W4", "-", "MinHeap", d.n(), measure(() -> priorityProcessing(d)));
    }

    private static void runBuildHeapComparison(CsvWriter csv, Data d) throws IOException {
        int[] descending = new int[d.n()];
        for (int i = 0; i < d.n(); i++) {
            descending[i] = d.n() - i;
        }
        record(csv, "BH", "random", "insert", d.n(), measure(() -> insertAll(d.values())));
        record(csv, "BH", "random", "buildHeap", d.n(), measure(() -> buildHeap(d.values())));
        record(csv, "BH", "descending", "insert", d.n(), measure(() -> insertAll(descending)));
        record(csv, "BH", "descending", "buildHeap", d.n(), measure(() -> buildHeap(descending)));
    }

    private static Measurement measure(Supplier<Measurement> run) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            run.get();
        }
        double[] times = new double[MEASURED_RUNS];
        Metrics metrics = null;
        for (int i = 0; i < MEASURED_RUNS; i++) {
            Measurement m = run.get();
            times[i] = m.timeMs();
            metrics = m.metrics();
        }
        Arrays.sort(times);
        return new Measurement(times[MEASURED_RUNS / 2], metrics);
    }

    private static void record(CsvWriter csv, String workload, String variant, String structure,
            int n, Measurement m) throws IOException {
        if (csv == null) {
            return;
        }
        csv.writeRow(workload, variant, structure, n, m.timeMs(), m.metrics());
        System.out.printf(Locale.US, "%-3s %-10s %-13s n=%-7d %12.4f ms   %s%n",
                workload, variant, structure, n, m.timeMs(), m.metrics());
    }

    private static IntList filled(Supplier<IntList> factory, int[] values) {
        IntList list = factory.get();
        for (int v : values) {
            list.add(v);
        }
        list.metrics().reset();
        return list;
    }

    private static Measurement randomAccess(Supplier<IntList> factory, Data d) {
        IntList list = filled(factory, d.values());
        long sum = 0;
        long t0 = System.nanoTime();
        for (int index : d.randomIndices()) {
            sum += list.get(index);
        }
        long t1 = System.nanoTime();
        blackhole += sum;
        return new Measurement(toMs(t1 - t0), list.metrics());
    }

    private static Measurement search(Supplier<IntList> factory, Data d) {
        IntList list = filled(factory, d.values());
        int found = 0;
        long t0 = System.nanoTime();
        for (int query : d.searchQueries()) {
            if (list.contains(query)) {
                found++;
            }
        }
        long t1 = System.nanoTime();
        if (found != W2_QUERIES / 2) {
            throw new IllegalStateException("W2: expected " + W2_QUERIES / 2 + " hits, got " + found);
        }
        return new Measurement(toMs(t1 - t0), list.metrics());
    }

    private static Measurement insertRemove(Supplier<IntList> factory, Data d, int index) {
        IntList list = filled(factory, d.values());
        long sum = 0;
        long t0 = System.nanoTime();
        for (int v : d.insertValues()) {
            list.add(index, v);
        }
        for (int i = 0; i < W3_OPERATIONS; i++) {
            sum += list.remove(index);
        }
        long t1 = System.nanoTime();
        if (list.size() != d.n()) {
            throw new IllegalStateException("W3: size changed to " + list.size());
        }
        blackhole += sum;
        return new Measurement(toMs(t1 - t0), list.metrics());
    }

    private static Measurement priorityProcessing(Data d) {
        MinHeap heap = new MinHeap();
        long t0 = System.nanoTime();
        for (int v : d.values()) {
            heap.insert(v);
        }
        int prev = Integer.MIN_VALUE;
        for (int i = 0; i < d.n(); i++) {
            int cur = heap.extractMin();
            if (cur < prev) {
                throw new IllegalStateException("W4: order broken, " + prev + " then " + cur);
            }
            prev = cur;
        }
        long t1 = System.nanoTime();
        blackhole += prev;
        return new Measurement(toMs(t1 - t0), heap.metrics());
    }

    private static Measurement insertAll(int[] values) {
        long t0 = System.nanoTime();
        MinHeap heap = new MinHeap();
        for (int v : values) {
            heap.insert(v);
        }
        long t1 = System.nanoTime();
        blackhole += heap.size();
        return new Measurement(toMs(t1 - t0), heap.metrics());
    }

    private static Measurement buildHeap(int[] values) {
        long t0 = System.nanoTime();
        MinHeap heap = MinHeap.buildHeap(values);
        long t1 = System.nanoTime();
        blackhole += heap.size();
        return new Measurement(toMs(t1 - t0), heap.metrics());
    }

    private static double toMs(long nanos) {
        return nanos / 1_000_000.0;
    }
}