# Java Assignment 4 (CO4) - Integrated Demonstration

## Run
Requires JDK 8+ (JDK 17 recommended).

```bash
javac JavaAssignment4.java
java JavaAssignment4
```

Tabs correspond to Tasks 1-6. Task 1 is a simulated fee transaction, Task 2 uses in-memory appointments, Task 3 uses sample book titles, and Task 5 uses illustrative fixed conversion rates.

## MySQL for Task 6
1. Run `campus_events.sql` in MySQL.
2. Add MySQL Connector/J JAR to your Java classpath.
3. Set environment variables `JAVA_ASSIGNMENT4_DB_URL`, `JAVA_ASSIGNMENT4_DB_USER`, and `JAVA_ASSIGNMENT4_DB_PASSWORD` (default URL: `jdbc:mysql://localhost:3306/campus_events`, default user: `root`).
4. Example on Windows CMD (replace the jar path):

```bat
javac -cp ".;mysql-connector-j-9.x.x.jar" JavaAssignment4.java
java -cp ".;mysql-connector-j-9.x.x.jar" JavaAssignment4
```

For Linux/macOS use `:` instead of `;` in the classpath.

## Test scenarios
- Task 1: valid/invalid amount, receipt, clear.
- Task 2: department changes doctor list; empty patient ID; close with unfinished form.
- Task 3: resize window; search; previous/next.
- Task 4: click dots; minimize and restore; clear.
- Task 5: try all four handler styles; press Enter; invalid amount.
- Task 6: valid registration; invalid ID; repeat same student/event; filter; pagination; CSV export of displayed page.

## Scope and limitations
This is one integrated demonstration, not the task-by-task repository structure required by the assignment. Written analyses, diagrams, pseudocode, screenshots, complexity comparisons, and progressive commits must be prepared separately. Task 1 does not make a real payment. Task 2 does not persist appointments. Task 3 uses sample data. Task 5 rates are illustrative. Task 6 needs a configured MySQL database and JDBC driver.
