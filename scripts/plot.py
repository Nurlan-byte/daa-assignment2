import csv
import math
from collections import defaultdict
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent.parent
RESULTS = ROOT / "results" / "results.csv"
BUILD_HEAP = ROOT / "results" / "buildheap.csv"
PLOTS = ROOT / "results" / "plots"

BLUE = "#2a78d6"
ORANGE = "#eb6834"
AQUA = "#1baf7a"
REFERENCE = "#8a8984"
INK = "#0b0b0b"
INK_SECONDARY = "#52514e"
GRID = "#e4e3df"
SURFACE = "#fcfcfb"

STRUCTURE_COLORS = {"DynamicArray": BLUE, "MyLinkedList": ORANGE}
METRIC_STYLES = {"steps": "-", "moves": "--", "comparisons": ":"}
METRIC_MARKERS = {"steps": "o", "moves": "s", "comparisons": "^"}

plt.rcParams.update(
    {
        "figure.facecolor": SURFACE,
        "axes.facecolor": SURFACE,
        "axes.edgecolor": GRID,
        "axes.labelcolor": INK_SECONDARY,
        "axes.titlecolor": INK,
        "axes.titlesize": 12,
        "axes.labelsize": 10,
        "xtick.color": INK_SECONDARY,
        "ytick.color": INK_SECONDARY,
        "legend.frameon": False,
        "legend.fontsize": 9,
        "font.size": 10,
        "lines.linewidth": 2,
        "lines.markersize": 6,
        "lines.solid_capstyle": "round",
        "lines.solid_joinstyle": "round",
    }
)


def read_rows(path):
    with open(path, newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
    for row in rows:
        row["n"] = int(row["n"])
        row["time_ms"] = float(row["time_ms"])
        for key in ("steps", "moves", "comparisons"):
            row[key] = int(row[key])
    return rows


def series(rows, **filters):
    picked = [r for r in rows if all(r[k] == v for k, v in filters.items())]
    return sorted(picked, key=lambda r: r["n"])


def style_axes(ax, title, ylabel):
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_title(title, loc="left", pad=10)
    ax.set_xlabel("n (number of elements, log scale)")
    ax.set_ylabel(ylabel)
    ax.grid(True, which="major", color=GRID, linewidth=1, linestyle="-")
    ax.set_axisbelow(True)
    for side in ("top", "right"):
        ax.spines[side].set_visible(False)
    ax.legend(loc="upper left", handlelength=4)


def plot_line(ax, points, value, label, color, linestyle="-", marker="o"):
    xs = [p["n"] for p in points]
    ys = [p[value] for p in points]
    if not xs or any(y <= 0 for y in ys):
        return
    ax.plot(
        xs,
        ys,
        label=label,
        color=color,
        linestyle=linestyle,
        marker=marker,
        markeredgecolor=SURFACE,
        markeredgewidth=1.5,
    )


def save(fig, name):
    PLOTS.mkdir(parents=True, exist_ok=True)
    fig.tight_layout()
    fig.savefig(PLOTS / name, dpi=150)
    plt.close(fig)
    print("saved", PLOTS / name)


def list_workload_figure(rows, workload, title, metrics, variant="-"):
    fig, (time_ax, ops_ax) = plt.subplots(1, 2, figsize=(12, 4.6))
    for structure, color in STRUCTURE_COLORS.items():
        points = series(rows, workload=workload, variant=variant, structure=structure)
        plot_line(time_ax, points, "time_ms", structure, color)
        for metric in metrics:
            plot_line(
                ops_ax,
                points,
                metric,
                f"{structure} - {metric}",
                color,
                METRIC_STYLES[metric],
                METRIC_MARKERS[metric],
            )
    style_axes(time_ax, f"{title}: time vs n", "Time, ms (median of 5 runs, log scale)")
    style_axes(ops_ax, f"{title}: operations vs n", "Operations (count, log scale)")
    return fig


def plot_w3(rows):
    fig, axes = plt.subplots(2, 2, figsize=(12, 9))
    for row_axes, variant in zip(axes, ("head", "middle")):
        time_ax, ops_ax = row_axes
        for structure, color in STRUCTURE_COLORS.items():
            points = series(rows, workload="W3", variant=variant, structure=structure)
            plot_line(time_ax, points, "time_ms", structure, color)
            for metric in ("steps", "moves"):
                plot_line(
                    ops_ax,
                    points,
                    metric,
                    f"{structure} - {metric}",
                    color,
                    METRIC_STYLES[metric],
                    METRIC_MARKERS[metric],
                )
        label = "index 0" if variant == "head" else "index n/2"
        style_axes(
            time_ax,
            f"W3 {variant} ({label}): time vs n",
            "Time, ms (median of 5 runs, log scale)",
        )
        style_axes(
            ops_ax,
            f"W3 {variant} ({label}): operations vs n",
            "Operations (count, log scale)",
        )
    save(fig, "w3_insert_remove.png")


def plot_w4(rows):
    fig, (time_ax, ops_ax) = plt.subplots(1, 2, figsize=(12, 4.6))
    points = series(rows, workload="W4", structure="MinHeap")
    plot_line(time_ax, points, "time_ms", "MinHeap", BLUE)
    for metric, color in (("steps", BLUE), ("moves", ORANGE), ("comparisons", AQUA)):
        plot_line(
            ops_ax,
            points,
            metric,
            f"MinHeap - {metric}",
            color,
            "-",
            METRIC_MARKERS[metric],
        )
    ns = [p["n"] for p in points]
    ops_ax.plot(
        ns,
        [n * math.log2(n) for n in ns],
        label="reference: n·log₂n",
        color=REFERENCE,
        linestyle="--",
        linewidth=1.5,
    )
    style_axes(
        time_ax,
        "W4 priority processing: time vs n",
        "Time, ms (median of 5 runs, log scale)",
    )
    style_axes(
        ops_ax,
        "W4 priority processing: operations vs n",
        "Operations (count, log scale)",
    )
    save(fig, "w4_priority.png")


def plot_build_heap(rows):
    fig, (ops_ax, time_ax) = plt.subplots(1, 2, figsize=(12, 4.6))
    for method, color in (("insert", ORANGE), ("buildHeap", BLUE)):
        for data, linestyle in (("random", "-"), ("descending", "--")):
            points = series(rows, workload="BH", variant=data, structure=method)
            label = (
                f"{'n × insert' if method == 'insert' else 'buildHeap'} - {data} input"
            )
            plot_line(ops_ax, points, "comparisons", label, color, linestyle)
            plot_line(time_ax, points, "time_ms", label, color, linestyle)
    style_axes(
        ops_ax,
        "Bonus B: comparisons, buildHeap vs n inserts",
        "Comparisons (count, log scale)",
    )
    style_axes(
        time_ax,
        "Bonus B: time, buildHeap vs n inserts",
        "Time, ms (median of 5 runs, log scale)",
    )
    save(fig, "bonus_buildheap.png")


def main():
    rows = read_rows(RESULTS)
    save(
        list_workload_figure(rows, "W1", "W1 random access", ["steps"]),
        "w1_random_access.png",
    )
    save(
        list_workload_figure(
            rows, "W2", "W2 search (steps = comparisons)", ["comparisons"]
        ),
        "w2_search.png",
    )
    plot_w3(rows)
    plot_w4(rows)
    if BUILD_HEAP.exists():
        plot_build_heap(read_rows(BUILD_HEAP))


if __name__ == "__main__":
    main()
