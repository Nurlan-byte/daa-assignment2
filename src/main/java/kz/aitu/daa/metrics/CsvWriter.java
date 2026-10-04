package kz.aitu.daa.metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class CsvWriter implements AutoCloseable {

    public static final String HEADER = "workload,variant,structure,n,time_ms,steps,moves,comparisons";

    private final BufferedWriter out;

    public CsvWriter(Path path) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        out = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        out.write(HEADER);
        out.write('\n');
    }

    public void writeRow(String workload, String variant, String structure, int n,
            double timeMs, Metrics metrics) throws IOException {
        out.write(String.format(Locale.US, "%s,%s,%s,%d,%.4f,%d,%d,%d",
                workload, variant, structure, n, timeMs,
                metrics.getSteps(), metrics.getMoves(), metrics.getComparisons()));
        out.write('\n');
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}