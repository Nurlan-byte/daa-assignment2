package kz.aitu.daa;

import kz.aitu.daa.metrics.CsvWriter;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvWriterTest {

    @Test
    void writesHeaderAndRowsWithDotDecimalsInAnyLocale() throws IOException {
        Locale original = Locale.getDefault();
        Path dir = Files.createTempDirectory("csv-test");
        Path file = dir.resolve("nested").resolve("results.csv");
        try {
            Locale.setDefault(Locale.forLanguageTag("ru-RU"));

            Metrics metrics = new Metrics();
            metrics.step();
            metrics.step();
            metrics.move();
            metrics.compare();

            try (CsvWriter csv = new CsvWriter(file)) {
                csv.writeRow("W1", "-", "DynamicArray", 100, 12.5, metrics);
                csv.writeRow("W3", "head", "MyLinkedList", 1000, 0.25, new Metrics());
            }

            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            assertEquals(3, lines.size());
            assertEquals(CsvWriter.HEADER, lines.get(0));
            assertEquals("W1,-,DynamicArray,100,12.5000,2,1,1", lines.get(1));
            assertEquals("W3,head,MyLinkedList,1000,0.2500,0,0,0", lines.get(2));
        } finally {
            Locale.setDefault(original);
            Files.deleteIfExists(file);
            Files.deleteIfExists(file.getParent());
            Files.deleteIfExists(dir);
        }
    }
}