# DAA Assignment 2 - Data Structures

`DynamicArray`, `MyLinkedList` and `MinHeap` implemented from scratch in Java (primitive `int`
storage, no `java.util` collections), with operation counters, a benchmark on four workloads and
JUnit 5 tests.

**Author:** Nurlan Yusupov, group SE-2526

The complexity table, loop invariant proofs, plots and discussion are in [REPORT.md](REPORT.md).

## Requirements

- JDK 17 or newer
- Maven 3.8 or newer
- Python 3 with `matplotlib` (only to redraw the plots)

## Project layout

```
src/main/java/kz/aitu/daa/structures   IntList, DynamicArray, MyLinkedList, MinHeap
src/main/java/kz/aitu/daa/metrics      Metrics (counters), CsvWriter
src/main/java/kz/aitu/daa/bench        Benchmark (workloads W1-W4 + buildHeap bonus)
src/test/java/kz/aitu/daa              JUnit 5 tests
scripts/plot.py                        draws the PNG charts from the CSV files
results/results.csv                    benchmark results (W1-W4)
results/buildheap.csv                  bonus B: buildHeap vs n inserts
results/plots/                         PNG charts
REPORT.md                              report
```

## How to run

Build and run all tests:

```bash
mvn clean test
```

Run the benchmark (writes `results/results.csv` and `results/buildheap.csv`):

```bash
mvn -q compile exec:java
```

Redraw the plots from the CSV files (writes PNG files to `results/plots/`):

```bash
pip install matplotlib
python scripts/plot.py
```

## Benchmark setup

- Sizes: n = 100, 1 000, 10 000, 100 000.
- All data comes from `new Random(42)`, so every run and every machine gets the same input and
  the same counter values. Both lists are filled with the same `n` values.
- W1: 10 000 `get(index)` calls with random indices.
- W2: 1 000 `contains(x)` queries, 500 values that are present and 500 negative values that are
  guaranteed to be absent, shuffled. The benchmark checks that exactly 500 are found.
- W3: 1 000 `add(index, x)` followed by 1 000 `remove(index)`, with index `0` (head) and `n / 2`
  (middle).
- W4: `n` calls of `insert`, then `n` calls of `extractMin`; the benchmark checks that the output
  is non-decreasing.
- Filling a list before W1-W3 is not measured: time and counters cover only the workload itself.
- Every case runs 5 warm-up times (discarded) and then 5 measured times; the CSV stores the
  median time. Before the first measurement all workloads run 3 times on n = 10 000 so that the
  JIT compiler has already compiled the hot methods.

## Counting rules

Counters are incremented inside the operations (`Metrics.step()`, `move()`, `compare()`), not
estimated afterwards. Index bounds checks and loop-counter conditions are not counted.

| Counter | DynamicArray | MyLinkedList | MinHeap |
|---|---|---|---|
| **steps** | one read of an array cell (`get`, `contains`, the removed value in `remove`) | one move to the next/previous node, or one read of a node value | one level visited on the path up (`siftUp`) or down (`siftDown`), one read of the root |
| **moves** | one element shifted by `add(index, x)` / `remove(index)`, one element copied during resize | one link update (`next`, `prev`, `head`, `tail`) | one array write in a swap (a swap = 2 moves), moving the last element to the root, one element copied during resize |
| **comparisons** | `data[i] == x` in `contains` | `node.val == x` in `contains` | one comparison of two heap elements |

Writing a brand-new element into the first free array cell (`add(x)`, `insert`) is not a move,
because no existing element is relocated. A shift is counted as one move, without an extra step
for reading the neighbour cell.

## CSV format

```
workload,variant,structure,n,time_ms,steps,moves,comparisons
```

`variant` is `head` or `middle` for W3 and `-` for the other workloads. `time_ms` is the median of
5 runs in milliseconds. In `buildheap.csv` the variant is the input order (`random` or
`descending`) and the structure column is the method (`insert` or `buildHeap`).

## Git workflow

- `main` holds only working code and is tagged `v1.0`.
- Feature branches: `feature/metrics`, `feature/array`, `feature/list`, `feature/heap`, merged
  into `main` with `--no-ff`.