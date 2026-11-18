import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * Java Assignment 5 (CO5) - integrated demonstration.
 * Compile: javac Assignment5.java
 * Run:     java Assignment5
 *
 * JDBC tasks need MySQL Connector/J on the classpath and a database configured
 * with DB_URL, DB_USER and DB_PASSWORD environment variables.
 */
public class Assignment5 {
    private static final String DB_URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:mysql://localhost:3306/java_assignment5");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== JAVA ASSIGNMENT 5 - CO5 =====");
            System.out.println("1. Bus-pass file store (A1/B1)");
            System.out.println("2. Buffered vs unbuffered line counter (A2/B1)");
            System.out.println("3. Generic DAO demonstration (A3)");
            System.out.println("4. Object serialization (A4)");
            System.out.println("5. JDBC transaction and safe login (A5/B2)");
            System.out.println("6. Statement / PreparedStatement / batch benchmark (B3)");
            System.out.println("7. Migrate pass CSV and inspect database metadata (B4)");
            System.out.println("8. Run file-store and buffering sample tests");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> fileStoreDemo(sc);
                    case "2" -> lineCounterDemo(sc);
                    case "3" -> daoDemo();
                    case "4" -> serializationDemo();
                    case "5" -> jdbcSafetyDemo(sc);
                    case "6" -> batchBenchmark(sc);
                    case "7" -> migrationAndMetadata(sc);
                    case "8" -> sampleTests();
                    case "0" -> { System.out.println("Exited."); return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception ex) {
                System.out.println("Operation failed: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

    // A1 / B1: CSV bus-pass store. CSV fields are escaped by quoting fields
    // containing commas, quotes or line breaks.
    static class PassStore {
        private final Path file;
        PassStore(Path file) { this.file = file; }

        void add(String passId, String regNo, String name, int route, double amount)
                throws IOException {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                w.write(csv(passId) + "," + csv(regNo) + "," + csv(name) + ","
                        + route + "," + amount);
                w.newLine();
            }
        }

        String[] find(String passId) throws IOException {
            if (!Files.exists(file)) return null;
            try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    try {
                        List<String> fields = parseCsvLine(line);
                        if (fields.size() != 5) continue; // damaged row: skip
                        if (fields.get(0).equals(passId)) return fields.toArray(new String[0]);
                    } catch (IllegalArgumentException ignored) {
                        // Malformed CSV row: skip and continue searching.
                    }
                }
            }
            return null;
        }

        List<String[]> listByRoute(int route) throws IOException {
            List<String[]> result = new ArrayList<>();
            if (!Files.exists(file)) return result;
            try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    try {
                        List<String> f = parseCsvLine(line);
                        if (f.size() == 5 && Integer.parseInt(f.get(3)) == route)
                            result.add(f.toArray(new String[0]));
                    } catch (RuntimeException ignored) { }
                }
            }
            return result;
        }

        private static String csv(String value) {
            if (value == null) value = "";
            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                return "\"" + value.replace("\"", "\"\"") + "\"";
            }
            return value;
        }

        // Small CSV parser supporting quoted commas and escaped double quotes.
        private static List<String> parseCsvLine(String line) {
            List<String> out = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean quoted = false;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '"') {
                    if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"'); i++;
                    } else quoted = !quoted;
                } else if (c == ',' && !quoted) {
                    out.add(field.toString()); field.setLength(0);
                } else field.append(c);
            }
            if (quoted) throw new IllegalArgumentException("Unclosed quoted field");
            out.add(field.toString());
            return out;
        }
    }

    private static void fileStoreDemo(Scanner sc) throws IOException {
        System.out.print("CSV file path (Enter for passes.csv): ");
        String p = sc.nextLine().trim();
        Path path = p.isEmpty() ? Path.of("passes.csv") : Path.of(p);
        PassStore store = new PassStore(path);
        System.out.println("1 Add  2 Find  3 List by route");
        String op = sc.nextLine().trim();
        switch (op) {
            case "1" -> {
                System.out.print("Pass ID: "); String id = sc.nextLine();
                System.out.print("Register no: "); String reg = sc.nextLine();
                System.out.print("Name: "); String name = sc.nextLine();
                System.out.print("Route: "); int route = Integer.parseInt(sc.nextLine());
                System.out.print("Amount: "); double amount = Double.parseDouble(sc.nextLine());
                if (id.isBlank() || reg.isBlank() || amount < 0) throw new IllegalArgumentException("Invalid fields");
                store.add(id, reg, name, route, amount);
                System.out.println("Pass saved.");
            }
            case "2" -> {
                System.out.print("Pass ID: ");
                System.out.println(Arrays.toString(store.find(sc.nextLine())));
            }
            case "3" -> {
                System.out.print("Route: "); int route = Integer.parseInt(sc.nextLine());
                for (String[] row : store.listByRoute(route)) System.out.println(Arrays.toString(row));
            }
            default -> System.out.println("Invalid operation.");
        }
    }

    // A2 / B1: compare byte-at-a-time and buffered line counting.
    static long countLinesUnbuffered(Path p) throws IOException {
        long lines = 0;
        boolean any = false, lastWasNewline = false;
        try (InputStream in = Files.newInputStream(p)) {
            int b;
            while ((b = in.read()) != -1) {
                any = true;
                if (b == '\n') lines++;
                lastWasNewline = b == '\n';
            }
        }
        if (any && !lastWasNewline) lines++;
        return lines;
    }

    static long countLinesBuffered(Path p) throws IOException {
        long lines = 0;
        boolean any = false, lastWasNewline = false;
        try (InputStream in = new BufferedInputStream(Files.newInputStream(p))) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                for (int i = 0; i < n; i++) {
                    any = true;
                    if (buffer[i] == '\n') lines++;
                    lastWasNewline = buffer[i] == '\n';
                }
            }
        }
        if (any && !lastWasNewline) lines++;
        return lines;
    }

    private static void lineCounterDemo(Scanner sc) throws IOException {
        System.out.print("File path: ");
        Path p = Path.of(sc.nextLine().trim());
        long t1 = System.nanoTime(), a = countLinesUnbuffered(p), t2 = System.nanoTime();
        long b = countLinesBuffered(p), t3 = System.nanoTime();
        System.out.printf("Unbuffered lines=%d time=%.3f ms%n", a, (t2-t1)/1e6);
        System.out.printf("Buffered   lines=%d time=%.3f ms%n", b, (t3-t2)/1e6);
    }

    // A3: generic DAO pattern, with a simple in-memory implementation for demo.
    interface Dao<T, K> {
        void insert(T value);
        Optional<T> findById(K id);
        List<T> findAll();
        boolean update(T value);
        boolean delete(K id);
    }
    static class Student {
        final int id; String name; String department;
        Student(int id, String name, String department) {
            this.id = id; this.name = name; this.department = department;
        }
        public String toString() { return id + " | " + name + " | " + department; }
    }
    static class StudentDao implements Dao<Student, Integer> {
        private final Map<Integer, Student> rows = new LinkedHashMap<>();
        public void insert(Student s) { rows.put(s.id, s); }
        public Optional<Student> findById(Integer id) { return Optional.ofNullable(rows.get(id)); }
        public List<Student> findAll() { return new ArrayList<>(rows.values()); }
        public boolean update(Student s) {
            if (!rows.containsKey(s.id)) return false;
            rows.put(s.id, s); return true;
        }
        public boolean delete(Integer id) { return rows.remove(id) != null; }
    }
    private static void daoDemo() {
        StudentDao dao = new StudentDao();
        dao.insert(new Student(101, "Arun", "CSE"));
        dao.insert(new Student(102, "Divya", "IT"));
        System.out.println("findById(101): " + dao.findById(101).orElse(null));
        System.out.println("findAll: " + dao.findAll());
        dao.update(new Student(102, "Divya S", "CSE"));
        System.out.println("after update: " + dao.findAll());
        dao.delete(101);
        System.out.println("after delete: " + dao.findAll());
    }

    // A4: serialize data only. Password and live Connection must not be persisted.
    static class Hostel implements Serializable {
        private static final long serialVersionUID = 1L;
        List<Room> rooms = new ArrayList<>();
    }
    static class Room implements Serializable {
        private static final long serialVersionUID = 1L;
        int roomNo; List<Resident> residents = new ArrayList<>();
        Room(int roomNo) { this.roomNo = roomNo; }
    }
    static class Resident implements Serializable {
        private static final long serialVersionUID = 1L;
        String name, registerNo, phone;
        transient String password;
        transient Connection liveConnection;
        Resident(String name, String registerNo, String phone, String password) {
            this.name = name; this.registerNo = registerNo; this.phone = phone; this.password = password;
        }
    }
    private static void serializationDemo() throws IOException, ClassNotFoundException {
        Hostel hostel = new Hostel();
        Room room = new Room(12);
        room.residents.add(new Resident("Arun", "192311001", "9876543210", "secret"));
        hostel.rooms.add(room);
        Path file = Path.of("hostel-data.ser");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(hostel);
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            Hostel loaded = (Hostel) in.readObject();
            Resident r = loaded.rooms.get(0).residents.get(0);
            System.out.println("Restored: " + r.name + ", " + r.registerNo + ", " + r.phone);
            System.out.println("Password after deserialization: " + r.password);
        }
        System.out.println("Serialized file: " + file.toAbsolutePath());
    }

    // A5 / B2: safe PreparedStatement login and atomic wallet transfer.
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    private static void jdbcSafetyDemo(Scanner sc) throws SQLException {
        try (Connection con = connect()) {
            System.out.println("1 Safe login test  2 Transfer ₹500 between wallets");
            String op = sc.nextLine().trim();
            if (op.equals("1")) {
                System.out.print("User ID: "); String id = sc.nextLine();
                System.out.print("Password: "); String pwd = sc.nextLine();
                String sql = "SELECT COUNT(*) FROM users WHERE id = ? AND pwd = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, id); ps.setString(2, pwd);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        System.out.println(rs.getInt(1) == 1 ? "Login accepted" : "Login rejected");
                    }
                }
            } else if (op.equals("2")) {
                con.setAutoCommit(false);
                try (PreparedStatement debit = con.prepareStatement(
                             "UPDATE wallet SET balance = balance - 500 WHERE wallet_id = ?");
                     PreparedStatement credit = con.prepareStatement(
                             "UPDATE wallet SET balance = balance + 500 WHERE wallet_id = ?")) {
                    debit.setInt(1, 1);
                    if (debit.executeUpdate() != 1) throw new SQLException("Source wallet not found");
                    credit.setInt(1, 2);
                    if (credit.executeUpdate() != 1) throw new SQLException("Destination wallet not found");
                    con.commit();
                    System.out.println("Transfer completed.");
                } catch (SQLException | RuntimeException e) {
                    con.rollback();
                    throw e;
                } finally {
                    con.setAutoCommit(true);
                }
            } else System.out.println("Invalid option.");
        }
    }

    // B3: compare Statement, PreparedStatement and prepared batch inserts.
    private static void batchBenchmark(Scanner sc) throws SQLException {
        System.out.print("Number of generated rows (e.g. 1000): ");
        int count = Integer.parseInt(sc.nextLine());
        try (Connection con = connect()) {
            createBusPassTable(con);
            long t1 = System.nanoTime();
            try (Statement st = con.createStatement()) {
                for (int i = 1; i <= count; i++) {
                    st.executeUpdate("INSERT INTO bus_pass(pass_id,reg_no,name,route_no,amount) VALUES("
                            + "'S" + i + "','R" + i + "','Student" + i + "'," + (i % 20 + 1) + ",4500.0)");
                }
            }
            long t2 = System.nanoTime();
            try (Statement clear = con.createStatement()) { clear.executeUpdate("DELETE FROM bus_pass"); }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO bus_pass(pass_id,reg_no,name,route_no,amount) VALUES(?,?,?,?,?)")) {
                for (int i = 1; i <= count; i++) {
                    bindPass(ps, i); ps.executeUpdate();
                }
            }
            long t3 = System.nanoTime();
            try (Statement clear = con.createStatement()) { clear.executeUpdate("DELETE FROM bus_pass"); }
            List<String[]> data = generatedPasses(count);
            long t4 = System.nanoTime();
            loadAll(con, data);
            long t5 = System.nanoTime();
            System.out.printf("Statement: %.3f ms%n", (t2-t1)/1e6);
            System.out.printf("PreparedStatement row-by-row: %.3f ms%n", (t3-t2)/1e6);
            System.out.printf("PreparedStatement batch + transaction: %.3f ms%n", (t5-t4)/1e6);
            System.out.println("Verify rows: " + rowCount(con));
            try (ResultSet rs = con.createStatement().executeQuery("SELECT * FROM bus_pass LIMIT 1")) {
                ResultSetMetaData md = rs.getMetaData();
                for (int i=1; i<=md.getColumnCount(); i++)
                    System.out.println(md.getColumnName(i) + " : " + md.getColumnTypeName(i));
            }
        }
    }

    static void createBusPassTable(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS bus_pass ("
                    + "pass_id VARCHAR(30) PRIMARY KEY, reg_no VARCHAR(30) NOT NULL,"
                    + "name VARCHAR(100) NOT NULL, route_no INT NOT NULL, amount DECIMAL(10,2) NOT NULL)");
        }
    }
    private static void bindPass(PreparedStatement ps, int i) throws SQLException {
        ps.setString(1, "S" + i); ps.setString(2, "R" + i);
        ps.setString(3, "Student" + i); ps.setInt(4, i % 20 + 1); ps.setDouble(5, 4500.0);
    }
    static List<String[]> generatedPasses(int count) {
        List<String[]> list = new ArrayList<>(count);
        for (int i=1; i<=count; i++)
            list.add(new String[]{"S"+i, "R"+i, "Student"+i, String.valueOf(i%20+1), "4500.0"});
        return list;
    }

    // Completed TODOs for PassLoader: parameters, addBatch, execute every 1000,
    // final partial batch, commit, rollback and restore auto-commit.
    static int loadAll(Connection con, List<String[]> passes) throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        int n = 0;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] p : passes) {
                if (p.length != 5) throw new SQLException("Invalid pass row: " + Arrays.toString(p));
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setDouble(5, Double.parseDouble(p[4]));
                ps.addBatch();
                n++;
                if (n % 1000 == 0) ps.executeBatch();
            }
            if (n % 1000 != 0) ps.executeBatch();
            con.commit();
            return n;
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    private static int rowCount(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) {
            rs.next(); return rs.getInt(1);
        }
    }

    // B4: migrate valid rows from CSV and report rejected rows; inspect metadata.
    private static void migrationAndMetadata(Scanner sc) throws SQLException, IOException {
        System.out.print("Source CSV path: ");
        Path p = Path.of(sc.nextLine().trim());
        List<String[]> valid = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line; int lineNo = 0;
            while ((line = r.readLine()) != null) {
                lineNo++;
                try {
                    List<String> f = parseCsv(line);
                    if (f.size() != 5) throw new IllegalArgumentException("expected 5 fields");
                    Integer.parseInt(f.get(3)); Double.parseDouble(f.get(4));
                    if (f.get(0).isBlank() || f.get(1).isBlank()) throw new IllegalArgumentException("missing ID");
                    valid.add(f.toArray(new String[0]));
                } catch (RuntimeException ex) {
                    rejected.add("Line " + lineNo + ": " + ex.getMessage() + " => " + line);
                }
            }
        }
        try (Connection con = connect()) {
            createBusPassTable(con);
            int loaded = loadAll(con, valid);
            System.out.println("Loaded records: " + loaded);
            System.out.println("Rejected records: " + rejected.size());
            rejected.forEach(System.out::println);
            DatabaseMetaData md = con.getMetaData();
            System.out.println("Database: " + md.getDatabaseProductName());
            System.out.println("Database version: " + md.getDatabaseProductVersion());
            System.out.println("Driver: " + md.getDriverName());
            System.out.println("Driver version: " + md.getDriverVersion());
            try (ResultSet tables = md.getTables(con.getCatalog(), null, "%", new String[]{"TABLE"})) {
                System.out.println("Tables:");
                while (tables.next()) System.out.println(" - " + tables.getString("TABLE_NAME"));
            }
        }
    }
    private static List<String> parseCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder b = new StringBuilder(); boolean q = false;
        for (int i=0; i<line.length(); i++) {
            char c=line.charAt(i);
            if (c=='"') {
                if (q && i+1<line.length() && line.charAt(i+1)=='"') { b.append('"'); i++; }
                else q=!q;
            } else if (c==',' && !q) { out.add(b.toString()); b.setLength(0); }
            else b.append(c);
        }
        if (q) throw new IllegalArgumentException("unclosed quote");
        out.add(b.toString()); return out;
    }

    private static void sampleTests() throws Exception {
        Path p = Files.createTempFile("pass-store", ".csv");
        PassStore store = new PassStore(p);
        store.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(p, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 7, 3800);
        System.out.println("find P1003: " + Arrays.toString(store.find("P1003")));
        System.out.println("find P1002: " + Arrays.toString(store.find("P1002")));
        System.out.println("route 7: " + store.listByRoute(7).size() + " record(s)");
        Files.deleteIfExists(p);
    }
}
