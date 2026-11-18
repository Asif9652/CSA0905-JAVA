package java_CSA0903.Java Assessment 3.FULL CODE;


import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

// =====================================================
// JAVA ASSIGNMENT 3 - CO3
// Exception Handling and Multithreading
// =====================================================

public class JavaAssignment3 {

    // =====================================================
    // Q1: ATM SIMULATION - EXCEPTION HANDLING
    // =====================================================

    static class ATM {
        private double balance = 10000;

        public void withdraw(String account, String amount) {
            try {
                int accountNo = Integer.parseInt(account);
                double money = Double.parseDouble(amount);

                if (accountNo <= 0)
                    throw new IllegalArgumentException("Invalid account number");

                if (money <= 0)
                    throw new IllegalArgumentException("Amount must be positive");

                if (money > balance)
                    throw new ArithmeticException("Insufficient balance");

                balance -= money;

                System.out.println("Withdrawal successful!");
                System.out.println("Withdrawn: Rs." + money);
                System.out.println("Remaining balance: Rs." + balance);

            } catch (NumberFormatException e) {
                System.out.println("Error: Enter valid numeric values");
            } catch (IllegalArgumentException | ArithmeticException e) {
                System.out.println("Error: " + e.getMessage());
            } finally {
                System.out.println("ATM transaction completed.");
            }
        }
    }

    static void question1() {
        System.out.println("\n========== Q1: ATM SIMULATION ==========");

        ATM atm = new ATM();

        atm.withdraw("12345", "2000");
        atm.withdraw("12345", "-500");
        atm.withdraw("12345", "20000");
        atm.withdraw("ABC", "500");
        atm.withdraw("12345", "500");
    }

    // =====================================================
    // Q2: ONLINE EXAMINATION PORTAL
    // USER-DEFINED EXCEPTIONS AND CHAINING
    // =====================================================

    static class ExamException extends Exception {
        ExamException(String message) {
            super(message);
        }

        ExamException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class ExamClosedException extends ExamException {
        ExamClosedException(String message) {
            super(message);
        }
    }

    static class StudentNotRegisteredException extends ExamException {
        StudentNotRegisteredException(String message) {
            super(message);
        }
    }

    static class SubmissionStorageException extends ExamException {
        SubmissionStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class ExamStorage {
        void save(String fileName, long sizeKb)
                throws SubmissionStorageException {

            try {
                if (sizeKb > 500)
                    throw new IOException("Disk quota exceeded");

                System.out.println("Saved " + fileName);

            } catch (IOException e) {
                throw new SubmissionStorageException(
                        "Could not store " + fileName, e);
            }
        }
    }

    static class ExamService {
        private final ExamStorage storage = new ExamStorage();

        void submit(String student, boolean registered,
                    boolean examOpen, String file, long size)
                throws ExamException {

            if (!examOpen)
                throw new ExamClosedException("Exam window is closed");

            if (!registered)
                throw new StudentNotRegisteredException(
                        "Student is not registered");

            storage.save(file, size);
        }
    }

    static void question2() {
        System.out.println("\n========== Q2: EXAMINATION PORTAL ==========");

        ExamService service = new ExamService();

        try {
            service.submit("Asif", true, true, "answer01.pdf", 120);

            service.submit("Ravi", true, true, "answer02.pdf", 800);

        } catch (ExamException e) {
            System.out.println("Error: " + e.getMessage());

            if (e.getCause() != null) {
                System.out.println("Cause: " +
                        e.getCause().getMessage());
            }
        }

        try {
            service.submit("Student2", false, true,
                    "answer03.pdf", 100);
        } catch (ExamException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            service.submit("Student3", true, false,
                    "answer04.pdf", 100);
        } catch (ExamException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // =====================================================
    // Q3: DAILY SALES REPORT
    // TRY-WITH-RESOURCES
    // =====================================================

    static void question3() {
        System.out.println("\n========== Q3: SALES REPORT ==========");

        Path report = null;

        try {
            Path file1 = Files.createTempFile("store1", ".txt");
            Path file2 = Files.createTempFile("store2", ".txt");
            report = Files.createTempFile("sales-report", ".txt");

            Files.write(file1, Arrays.asList("Store 1 Sales: 5000"));
            Files.write(file2, Arrays.asList("Store 2 Sales: 7000"));

            try (
                BufferedReader reader1 =
                        Files.newBufferedReader(file1);
                BufferedReader reader2 =
                        Files.newBufferedReader(file2);
                BufferedWriter writer =
                        Files.newBufferedWriter(report)
            ) {
                String line;

                writer.write("DAILY SALES REPORT");
                writer.newLine();

                while ((line = reader1.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }

                while ((line = reader2.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Report generated successfully:");

            try (BufferedReader reader =
                         Files.newBufferedReader(report)) {

                String line;

                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            Files.deleteIfExists(file1);
            Files.deleteIfExists(file2);

        } catch (IOException e) {
            System.out.println("File Error: " + e.getMessage());
        } finally {
            if (report != null) {
                try {
                    Files.deleteIfExists(report);
                } catch (IOException e) {
                    System.out.println("Cleanup error: " + e.getMessage());
                }
            }
        }

        System.out.println("All report resources closed.");
    }

    // =====================================================
    // Q4: THREAD CREATION AND LIFE CYCLE
    // =====================================================

    static class ScannerStation extends Thread {
        private final int stationId;
        private final int scripts;

        ScannerStation(int stationId, int scripts) {
            this.stationId = stationId;
            this.scripts = scripts;
        }

        @Override
        public void run() {
            try {
                System.out.println("Station " + stationId +
                        " started using Thread.");

                Thread.sleep(500);

                System.out.println("Station " + stationId +
                        " scanned " + scripts + " scripts.");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Station interrupted.");
            }
        }
    }

    static class RunnableStation implements Runnable {
        private final int stationId;
        private final int scripts;

        RunnableStation(int stationId, int scripts) {
            this.stationId = stationId;
            this.scripts = scripts;
        }

        @Override
        public void run() {
            try {
                System.out.println("Station " + stationId +
                        " started using Runnable.");

                Thread.sleep(300);

                System.out.println("Station " + stationId +
                        " scanned " + scripts + " scripts.");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static void question4() {
        System.out.println("\n========== Q4: MULTITHREADING ==========");

        Thread[] stations = new Thread[4];

        for (int i = 0; i < 4; i++) {
            stations[i] = new ScannerStation(i + 1, 10);
            stations[i].start();
        }

        for (Thread station : stations) {
            try {
                station.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("All Thread stations completed.");

        Thread t1 = new Thread(new RunnableStation(5, 15));
        Thread t2 = new Thread(new RunnableStation(6, 15));

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("All Runnable stations completed.");
    }

    // =====================================================
    // Q5: CINEMA SEAT BOOKING - SYNCHRONIZATION
    // =====================================================

    static class SeatBooking {
        private final boolean[] booked;
        private int bookingsMade = 0;

        SeatBooking(int seats) {
            booked = new boolean[seats];
        }

        // Atomic check-and-book operation
        public synchronized void book(int seat) {

            if (seat < 0 || seat >= booked.length)
                return;

            if (!booked[seat]) {
                booked[seat] = true;
                bookingsMade++;

                System.out.println("Seat " + seat +
                        " booked by " +
                        Thread.currentThread().getName());
            }
        }

        public int getBookingsMade() {
            return bookingsMade;
        }
    }

    static void question5() {
        System.out.println("\n========== Q5: CINEMA BOOKING ==========");

        SeatBooking screen = new SeatBooking(10);

        Thread[] users = new Thread[100];

        for (int i = 0; i < users.length; i++) {
            final int seat = i % 10;

            users[i] = new Thread(
                    () -> screen.book(seat),
                    "User-" + (i + 1)
            );

            users[i].start();
        }

        for (Thread t : users) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Seats sold: " +
                screen.getBookingsMade());

        System.out.println("Double booking prevented.");
    }

    // =====================================================
    // Q6 PART A: HOSTEL WALLET TRANSFER
    // DEADLOCK PREVENTION USING LOCK ORDERING
    // =====================================================

    static class Wallet {
        final int id;
        double balance;

        Wallet(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    static class WalletTransfer {

        // Global lock ordering prevents circular waiting
        private static final Object transferLock = new Object();

        static void transfer(Wallet from, Wallet to,
                             double amount) {

            synchronized (transferLock) {

                if (amount <= 0) {
                    System.out.println("Invalid transfer amount.");
                    return;
                }

                if (from.balance >= amount) {
                    from.balance -= amount;
                    to.balance += amount;

                    System.out.println("Transferred Rs." +
                            amount + " from Wallet " +
                            from.id + " to Wallet " + to.id);
                } else {
                    System.out.println("Insufficient wallet balance.");
                }
            }
        }
    }

    // Q6 PART B: PRODUCER-CONSUMER
    static class TokenCounter {
        private final int[] buffer = new int[5];

        private int count = 0;
        private int in = 0;
        private int out = 0;

        public synchronized void put(int token)
                throws InterruptedException {

            while (count == buffer.length) {
                wait();
            }

            buffer[in] = token;
            in = (in + 1) % buffer.length;
            count++;

            System.out.println("Produced: " + token +
                    " | Counter: " + count);

            notifyAll();
        }

        public synchronized int take()
                throws InterruptedException {

            while (count == 0) {
                wait();
            }

            int token = buffer[out];
            out = (out + 1) % buffer.length;
            count--;

            System.out.println("Consumed: " + token +
                    " | Counter: " + count);

            notifyAll();

            return token;
        }
    }

    static void question6() {
        System.out.println("\n========== Q6: WALLET AND TOKENS ==========");

        Wallet w1 = new Wallet(1, 5000);
        Wallet w2 = new Wallet(2, 3000);

        Thread transfer1 = new Thread(
                () -> WalletTransfer.transfer(w1, w2, 1000));

        Thread transfer2 = new Thread(
                () -> WalletTransfer.transfer(w2, w1, 500));

        transfer1.start();
        transfer2.start();

        try {
            transfer1.join();
            transfer2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Wallet 1 Balance: " + w1.balance);
        System.out.println("Wallet 2 Balance: " + w2.balance);

        TokenCounter counter = new TokenCounter();

        int[] collected = new int[2];

        Thread cook = new Thread(() -> {
            try {
                for (int t = 1; t <= 20; t++) {
                    counter.put(t);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread[] students = new Thread[2];

        for (int s = 0; s < 2; s++) {
            final int id = s;

            students[s] = new Thread(() -> {
                try {
                    for (int k = 0; k < 10; k++) {
                        collected[id] += counter.take();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        cook.start();

        for (Thread t : students) {
            t.start();
        }

        try {
            cook.join();

            for (Thread t : students) {
                t.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Sum of tokens collected: " +
                (collected[0] + collected[1]) +
                " (expected 210)");
    }

    // =====================================================
    // Q7: CONCURRENT RESULT PROCESSING
    // EXECUTORS, CALLABLE, FUTURE
    // =====================================================

    static class Result {
        final String fileName;
        final boolean success;
        final int marks;
        final String message;

        Result(String fileName, boolean success,
               int marks, String message) {
            this.fileName = fileName;
            this.success = success;
            this.marks = marks;
            this.message = message;
        }
    }

    static class ResultProcessor implements Callable<Result> {

        private final String fileName;

        ResultProcessor(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public Result call() {

            try {
                Thread.sleep(100);

                if (fileName.contains("missing")) {
                    throw new FileNotFoundException(
                            "Result file not found");
                }

                if (fileName.contains("corrupt")) {
                    throw new IOException(
                            "Corrupted result file");
                }

                // Simulated marks read from a valid file
                int marks = 80;

                return new Result(
                        fileName, true, marks,
                        "Processed successfully");

            } catch (Exception e) {
                return new Result(
                        fileName, false, 0,
                        e.getMessage());
            }
        }
    }

    static void processResults(int poolSize) {

        System.out.println("\nProcessing with " +
                poolSize + " threads");

        String[] files = {
                "student01.txt",
                "student02.txt",
                "student03.txt",
                "missing04.txt",
                "corrupt05.txt",
                "student06.txt",
                "student07.txt",
                "student08.txt",
                "student09.txt",
                "student10.txt"
        };

        ExecutorService executor =
                Executors.newFixedThreadPool(poolSize);

        ConcurrentMap<String, Result> results =
                new ConcurrentHashMap<>();

        List<Future<Result>> futures = new ArrayList<>();

        long start = System.nanoTime();

        try {
            // Submit all jobs
            for (String file : files) {
                futures.add(
                        executor.submit(
                                new ResultProcessor(file)));
            }

            // Collect completed results
            for (Future<Result> future : futures) {

                try {
                    Result result = future.get();

                    results.put(result.fileName, result);

                } catch (ExecutionException e) {
                    System.out.println(
                            "Unexpected task error: " +
                            e.getCause());
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Processing interrupted.");

        } finally {
            executor.shutdown();

            try {
                if (!executor.awaitTermination(
                        10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        long end = System.nanoTime();

        int success = 0;
        int failure = 0;
        int totalMarks = 0;

        for (Result result : results.values()) {

            if (result.success) {
                success++;
                totalMarks += result.marks;
                System.out.println(result.fileName +
                        ": " + result.message);
            } else {
                failure++;
                System.out.println(result.fileName +
                        ": FAILED - " + result.message);
            }
        }

        System.out.println("\nResult Summary");
        System.out.println("Total Files: " + files.length);
        System.out.println("Successful: " + success);
        System.out.println("Failed: " + failure);
        System.out.println("Total Marks: " + totalMarks);
        System.out.println("Execution Time: " +
                (end - start) / 1_000_000.0 + " ms");
    }

    static void question7() {
        System.out.println("\n========== Q7: RESULT PROCESSING ==========");

        processResults(1);
        processResults(2);
        processResults(4);
        processResults(8);
    }

    // =====================================================
    // MAIN METHOD
    // =====================================================

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("       JAVA ASSIGNMENT 3 - CO3");
        System.out.println("   EXCEPTION & MULTITHREADING PROJECT");
        System.out.println("========================================");

        question1();
        question2();
        question3();
        question4();
        question5();
        question6();
        question7();

        System.out.println("\n========================================");
        System.out.println("       ALL 7 QUESTIONS COMPLETED");
        System.out.println("========================================");
    }
}

