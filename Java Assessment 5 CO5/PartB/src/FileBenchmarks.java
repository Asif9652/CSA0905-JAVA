import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Reproducible local measurements; outputs vary by machine and cache state. */
public class FileBenchmarks {
    static long countUnbuffered(Path p) throws IOException {
        long lines = 0;
        try (InputStream in = new FileInputStream(p.toFile())) {
            int b;
            while ((b = in.read()) != -1) if (b == '\n') lines++;
        }
        return lines;
    }

    static long countBuffered(Path p) throws IOException {
        long lines = 0;
        byte[] buffer = new byte[64 * 1024];
        try (InputStream in = new BufferedInputStream(Files.newInputStream(p), buffer.length)) {
            int n;
            while ((n = in.read(buffer)) != -1)
                for (int i = 0; i < n; i++) if (buffer[i] == '\n') lines++;
        }
        return lines;
    }

    static void createFile(Path p, long bytes) throws IOException {
        byte[] block = new byte[64 * 1024];
        Arrays.fill(block, (byte)'x');
        for (int i = 79; i < block.length; i += 80) block[i] = '\n';
        try (OutputStream out = Files.newOutputStream(p)) {
            long remaining = bytes;
            while (remaining > 0) {
                int n = (int)Math.min(block.length, remaining);
                out.write(block, 0, n);
                remaining -= n;
            }
        }
    }

    static long timeMs(IORunnable action) throws IOException {
        long start = System.nanoTime();
        action.run();
        return (System.nanoTime() - start) / 1_000_000;
    }
    interface IORunnable { void run() throws IOException; }

    static void lookupGrowth() throws IOException {
        System.out.println("\nLookup growth (one scan; generated rows):");
        for (int n : new int[]{1_000, 10_000, 50_000, 100_000}) {
            Path p = Files.createTempFile("growth", ".csv");
            try {
                try (BufferedWriter w = Files.newBufferedWriter(p)) {
                    for (int i = 0; i < n; i++)
                        w.write("P" + i + ",R" + i + ",Student" + i + ",1,2027-12-31,100.0\n");
                }
                String target = "P" + (n - 1);
                long ms = timeMs(() -> {
                    try (BufferedReader r = Files.newBufferedReader(p)) {
                        String line;
                        while ((line = r.readLine()) != null) {
                            String[] f = PassStore.parseValid(line);
                            if (f != null && f[0].equals(target)) break;
                        }
                    }
                });
                System.out.printf("%d records: %d ms%n", n, ms);
            } finally { Files.deleteIfExists(p); }
        }
    }

    public static void main(String[] args) throws IOException {
        for (int mb : new int[]{1, 10, 50}) {
            Path p = Files.createTempFile("attendance-" + mb + "mb", ".log");
            try {
                createFile(p, mb * 1024L * 1024L);
                // Warm-up buffered path once; timings remain machine-dependent.
                countBuffered(p);
                long unbuffered = timeMs(() -> countUnbuffered(p));
                long buffered = timeMs(() -> countBuffered(p));
                System.out.printf("%d MB | unbuffered=%d ms | buffered=%d ms%n",
                        mb, unbuffered, buffered);
            } finally { Files.deleteIfExists(p); }
        }
        lookupGrowth();
    }
}
