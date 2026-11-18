import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** UTF-8 delimited bus-pass store used for the CO5 file persistence exercise. */
public class PassStore {
    private final Path file;
    public PassStore(Path file) { this.file = Objects.requireNonNull(file); }

    private static String encode(String value) {
        Objects.requireNonNull(value, "Pass fields cannot be null");
        if (value.isEmpty() || value.contains(",") || value.contains("\n") ||
                value.contains("\r") || value.contains("\"")) {
            throw new IllegalArgumentException("Fields must be non-empty and cannot contain comma, quote or newline");
        }
        return value;
    }

    public void add(String passId, String regNo, String name, int route,
                    String validityDate, double amount) throws IOException {
        if (route <= 0 || amount < 0 || !Double.isFinite(amount))
            throw new IllegalArgumentException("Invalid route or amount");
        String line = String.join(",",
                encode(passId), encode(regNo), encode(name), Integer.toString(route),
                encode(validityDate), Double.toString(amount));
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(line);
            w.newLine();
        }
    }

    /** Returns six fields for the matching ID, or null if absent. Malformed rows are skipped. */
    public String[] find(String passId) throws IOException {
        if (!Files.exists(file)) throw new NoSuchFileException(file.toString());
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] fields = parseValid(line);
                if (fields != null && fields[0].equals(passId)) return fields;
            }
        }
        return null;
    }

    public List<String[]> listByRoute(int route) throws IOException {
        if (!Files.exists(file)) throw new NoSuchFileException(file.toString());
        List<String[]> result = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] fields = parseValid(line);
                if (fields != null && Integer.parseInt(fields[3]) == route) result.add(fields);
            }
        }
        return result;
    }

    /** Six fields: passId, regNo, name, route, validityDate, amount. */
    public static String[] parseValid(String line) {
        if (line == null || line.isBlank()) return null;
        String[] f = line.split(",", -1);
        if (f.length != 6) return null;
        try {
            if (f[0].isBlank() || f[1].isBlank() || f[2].isBlank() || f[4].isBlank()) return null;
            if (Integer.parseInt(f[3]) <= 0) return null;
            double amount = Double.parseDouble(f[5]);
            if (!Double.isFinite(amount) || amount < 0) return null;
            return f;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        Path f = Files.createTempFile("passes", ".csv");
        try {
            PassStore store = new PassStore(f);
            store.add("P1001", "192311001", "Arun", 12, "2027-03-31", 4500);
            Files.writeString(f, "P1002,DAMAGED LINE\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            store.add("P1003", "192311003", "Divya", 7, "2027-03-31", 3800);
            System.out.println(Arrays.toString(store.find("P1003")));
            System.out.println(store.find("P1002"));
            System.out.println("Route 7 count: " + store.listByRoute(7).size());
        } finally {
            Files.deleteIfExists(f);
        }
    }
}
