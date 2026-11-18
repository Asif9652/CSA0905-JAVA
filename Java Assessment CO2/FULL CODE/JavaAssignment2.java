
import java.util.*;
import java.util.function.Supplier;

public class JavaAssignment2 {

    // =====================================================
    // Q1: STRING, STRINGBUILDER AND STRINGBUFFER
    // =====================================================

    static void question1() {
        System.out.println("\n========== Q1: STRING PERFORMANCE ==========");

        int tickets = 1000;
        int fields = 50;

        // String using +
        long start = System.nanoTime();
        for (int i = 0; i < tickets; i++) {
            String ticket = "";
            for (int j = 0; j < fields; j++) {
                ticket += "Field" + j + " ";
            }
        }
        long stringTime = System.nanoTime() - start;

        // StringBuilder
        start = System.nanoTime();
        for (int i = 0; i < tickets; i++) {
            StringBuilder ticket = new StringBuilder();
            for (int j = 0; j < fields; j++) {
                ticket.append("Field").append(j).append(" ");
            }
            ticket.toString();
        }
        long builderTime = System.nanoTime() - start;

        // StringBuffer
        start = System.nanoTime();
        for (int i = 0; i < tickets; i++) {
            StringBuffer ticket = new StringBuffer();
            for (int j = 0; j < fields; j++) {
                ticket.append("Field").append(j).append(" ");
            }
            ticket.toString();
        }
        long bufferTime = System.nanoTime() - start;

        System.out.println("Tickets: " + tickets);
        System.out.println("Fields per ticket: " + fields);
        System.out.println("String (+): " + stringTime / 1_000_000.0 + " ms");
        System.out.println("StringBuilder: " + builderTime / 1_000_000.0 + " ms");
        System.out.println("StringBuffer: " + bufferTime / 1_000_000.0 + " ms");

        System.out.println("Decision:");
        System.out.println("StringBuilder: Best for individual ticket construction.");
        System.out.println("StringBuffer: Useful for shared synchronized text operations.");
        System.out.println("String: Immutable and creates intermediate objects.");
    }

    // =====================================================
    // Q2: ARRAYLIST, LINKEDLIST AND ARRAYDEQUE
    // =====================================================

    static void question2() {
        System.out.println("\n========== Q2: LIST PERFORMANCE ==========");

        int[] sizes = {1000, 5000, 10000};

        for (int size : sizes) {
            ArrayList<Integer> arrayList = new ArrayList<>();
            LinkedList<Integer> linkedList = new LinkedList<>();
            ArrayDeque<Integer> deque = new ArrayDeque<>();

            long start = System.nanoTime();

            for (int i = 0; i < size; i++)
                arrayList.add(i);

            long arrayTime = System.nanoTime() - start;

            start = System.nanoTime();

            for (int i = 0; i < size; i++)
                linkedList.add(i);

            long linkedTime = System.nanoTime() - start;

            start = System.nanoTime();

            for (int i = 0; i < size; i++)
                deque.addLast(i);

            long dequeTime = System.nanoTime() - start;

            start = System.nanoTime();
            arrayList.get(size / 2);
            long arrayAccess = System.nanoTime() - start;

            start = System.nanoTime();
            linkedList.get(size / 2);
            long linkedAccess = System.nanoTime() - start;

            System.out.println("\nSize: " + size);
            System.out.println("ArrayList insertion: " + arrayTime + " ns");
            System.out.println("LinkedList insertion: " + linkedTime + " ns");
            System.out.println("ArrayDeque insertion: " + dequeTime + " ns");
            System.out.println("ArrayList middle access: " + arrayAccess + " ns");
            System.out.println("LinkedList middle access: " + linkedAccess + " ns");
        }

        ArrayDeque<String> upNext = new ArrayDeque<>();
        upNext.addLast("Song A");
        upNext.addLast("Song B");
        upNext.addFirst("Priority Song");
        System.out.println("\nUp Next Queue: " + upNext);
        System.out.println("Playing: " + upNext.removeFirst());

        System.out.println("Decision: ArrayList for random access.");
        System.out.println("ArrayDeque for adding/removing at both ends.");
    }

    // =====================================================
    // Q3: HASHSET AND EQUALS-HASHCODE CONTRACT
    // =====================================================

    static class Student {
        final String regNo;
        final String name;

        Student(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;

            if (!(obj instanceof Student))
                return false;

            Student other = (Student) obj;
            return regNo.equals(other.regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regNo);
        }

        @Override
        public String toString() {
            return regNo + " " + name;
        }
    }

    static void question3() {
        System.out.println("\n========== Q3: HASHSET ==========");

        Set<Student> attendance = new HashSet<>();

        attendance.add(new Student("192311001", "Arun"));
        attendance.add(new Student("192311002", "Divya"));
        attendance.add(new Student("192311001", "Arun"));

        System.out.println("Unique students: " + attendance.size());
        System.out.println("Attendance: " + attendance);

        // Additional test
        Student s1 = new Student("192311003", "Ravi");
        Student s2 = new Student("192311003", "Ravi");

        System.out.println("Are students equal? " + s1.equals(s2));
        System.out.println("Same hash code? " +
                (s1.hashCode() == s2.hashCode()));

        System.out.println("Decision: hashCode() and equals() must agree.");
    }

    // =====================================================
    // Q4: HASHMAP, TREEMAP AND LINKEDHASHMAP
    // =====================================================

    static void question4() {
        System.out.println("\n========== Q4: MAP COLLECTIONS ==========");

        Map<String, Integer> hashMap = new HashMap<>();
        Map<String, Integer> treeMap = new TreeMap<>();
        Map<String, Integer> linkedMap = new LinkedHashMap<>();

        String[] codes = {"MED103", "MED101", "MED105", "MED102", "MED104"};

        int[] stock = {30, 50, 20, 40, 60};

        for (int i = 0; i < codes.length; i++) {
            hashMap.put(codes[i], stock[i]);
            treeMap.put(codes[i], stock[i]);
            linkedMap.put(codes[i], stock[i]);
        }

        System.out.println("HashMap: " + hashMap);
        System.out.println("TreeMap (Code order): " + treeMap);
        System.out.println("LinkedHashMap (Insertion order): " + linkedMap);

        System.out.println("Stock of MED103: " + hashMap.get("MED103"));

        // Lookup experiment
        int lookups = 100000;
        long start = System.nanoTime();

        for (int i = 0; i < lookups; i++)
            hashMap.get("MED103");

        long time = System.nanoTime() - start;

        System.out.println("100000 HashMap lookups: " + time + " ns");

        System.out.println("Decision:");
        System.out.println("HashMap: Fast average lookup.");
        System.out.println("TreeMap: Sorted key order.");
        System.out.println("LinkedHashMap: Insertion order.");
    }

    // =====================================================
    // Q5: GENERICS AND COMPARATOR - LEADERBOARD
    // =====================================================

    static class Team {
        final String name;
        final int points;
        final int goalDiff;

        Team(String name, int points, int goalDiff) {
            this.name = name;
            this.points = points;
            this.goalDiff = goalDiff;
        }

        @Override
        public String toString() {
            return name + "(" + points + "," + goalDiff + ")";
        }
    }

    static class Leaderboard<T> {
        private final List<T> items = new ArrayList<>();
        private final Comparator<? super T> order;

        Leaderboard(Comparator<? super T> order) {
            this.order = order;
        }

        void add(T item) {
            items.add(item);
        }

        List<T> ranking() {
            List<T> result = new ArrayList<>(items);
            result.sort(order);
            return result;
        }
    }

    static void question5() {
        System.out.println("\n========== Q5: LEADERBOARD ==========");

        Comparator<Team> byRank =
                Comparator.comparingInt((Team t) -> t.points)
                        .reversed()
                        .thenComparing(
                                Comparator.comparingInt(
                                        (Team t) -> t.goalDiff).reversed())
                        .thenComparing(t -> t.name);

        Leaderboard<Team> board = new Leaderboard<>(byRank);

        board.add(new Team("Panthers", 9, 4));
        board.add(new Team("Eagles", 9, 6));
        board.add(new Team("Cobras", 6, 1));
        board.add(new Team("Bisons", 9, 4));

        System.out.println("Team Ranking: " + board.ranking());

        System.out.println("Decision:");
        System.out.println("Higher points receive higher ranking.");
        System.out.println("Goal difference breaks ties.");
        System.out.println("Team name breaks remaining ties.");
    }

    // =====================================================
    // Q6: HOSPITAL EMERGENCY TRIAGE - PRIORITY QUEUE
    // =====================================================

    static class Patient {
        final String name;
        final int severity;
        final long arrival;

        Patient(String name, int severity, long arrival) {
            this.name = name;
            this.severity = severity;
            this.arrival = arrival;
        }

        @Override
        public String toString() {
            return name + "(S" + severity + ")";
        }
    }

    static class Triage {
        private long counter = 0;

        private final PriorityQueue<Patient> queue =
                new PriorityQueue<>(
                        Comparator.comparingInt(
                                (Patient p) -> p.severity)
                                .thenComparingLong(p -> p.arrival));

        void admit(String name, int severity) {
            if (severity < 1 || severity > 5) {
                System.out.println("Invalid severity: " + name);
                return;
            }

            queue.add(new Patient(name, severity, counter++));
        }

        Patient next() {
            return queue.poll();
        }

        void displayWaitingList() {
            List<Patient> waiting = new ArrayList<>(queue);

            waiting.sort(
                    Comparator.comparingInt((Patient p) -> p.severity)
                            .thenComparingLong(p -> p.arrival));

            System.out.println("Waiting Patients: " + waiting);
        }
    }

    static void question6() {
        System.out.println("\n========== Q6: HOSPITAL TRIAGE ==========");

        Triage er = new Triage();

        er.admit("Ravi", 3);
        er.admit("Meena", 1);
        er.admit("John", 3);
        er.admit("Asha", 5);
        er.admit("Kumar", 1);
        er.admit("Lata", 2);
        er.admit("Priya", 4);
        er.admit("Arun", 2);

        er.displayWaitingList();

        System.out.println("Treatment Order:");

        Patient p;
        while ((p = er.next()) != null) {
            System.out.print(p + " ");
        }

        System.out.println();
        System.out.println("Decision: PriorityQueue treats the most severe patient first.");
        System.out.println("Arrival counter maintains FIFO order for equal severity.");
    }

    // =====================================================
    // Q7: PERFORMANCE BOTTLENECK AND REDESIGN
    // =====================================================

    static void question7() {
        System.out.println("\n========== Q7: REDESIGN ==========");

        int size = 50000;

        Map<String, Integer> medicineMap = new HashMap<>();
        List<String> medicineList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            String code = "MED" + i;
            medicineMap.put(code, i);
            medicineList.add(code);
        }

        String searchCode = "MED49999";

        // Original solution: linear search
        long start = System.nanoTime();

        boolean found = medicineList.contains(searchCode);

        long linearTime = System.nanoTime() - start;

        // Redesigned solution: HashMap lookup
        start = System.nanoTime();

        boolean mapFound = medicineMap.containsKey(searchCode);

        long mapTime = System.nanoTime() - start;

        System.out.println("Medicines: " + size);
        System.out.println("Linear search found: " + found);
        System.out.println("HashMap search found: " + mapFound);

        System.out.println("Linear Search Time: " + linearTime + " ns");
        System.out.println("HashMap Search Time: " + mapTime + " ns");

        System.out.println("Original complexity: O(n)");
        System.out.println("Redesigned average complexity: O(1)");
        System.out.println("Extra space: HashMap stores key-value entries.");

        System.out.println("Conclusion:");
        System.out.println("HashMap reduces repeated linear searching.");
        System.out.println("It is suitable for frequent medicine code lookups.");
    }

    // =====================================================
    // MAIN METHOD - EXECUTES ALL 7 QUESTIONS
    // =====================================================

    public static void main(String[] args) {

        System.out.println("==========================================");
        System.out.println("       JAVA ASSIGNMENT 2 - CO2");
        System.out.println("       COLLECTION FRAMEWORK PROJECT");
        System.out.println("==========================================");

        question1();
        question2();
        question3();
        question4();
        question5();
        question6();
        question7();

        System.out.println("\n==========================================");
        System.out.println("       ALL 7 QUESTIONS COMPLETED");
        System.out.println("==========================================");
    }
}
