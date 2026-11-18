# Java Assignment 5 – CO5

## Requirements
- JDK 17 or later (uses switch expressions and `Path.of`)
- MySQL Server for JDBC tasks
- MySQL Connector/J driver for JDBC tasks

## Compile and run
```bash
javac Assignment5.java
java Assignment5
```

JDBC configuration (Windows PowerShell):
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/java_assignment5"
$env:DB_USER="root"
$env:DB_PASSWORD="your_mysql_password"
javac -cp ".;mysql-connector-j-8.x.x.jar" Assignment5.java
java -cp ".;mysql-connector-j-8.x.x.jar" Assignment5
```
Replace the JAR name with the actual downloaded Connector/J filename.

Run `java_assignment5.sql` in MySQL Workbench before selecting JDBC menu options.

## Menu
1. CSV bus-pass add/find/list
2. Buffered and unbuffered line counter
3. Generic DAO pattern demonstration (in-memory implementation)
4. Serializable Hostel object graph; password and Connection fields are transient
5. PreparedStatement login and transactional wallet transfer
6. Statement / PreparedStatement / batch insert timing and metadata
7. CSV migration with validation report and DatabaseMetaData
8. Sample file-store tests including a damaged row

## Important assignment evidence still to collect
- A1: text and binary record layouts, pseudocode, damaged/missing/incomplete-file analysis.
- A2: benchmark 1 MB, 10 MB, 50 MB files; repeat runs and record actual timings.
- A3: include DAO class diagram and object-to-table mapping in the report.
- A4: explain object graph serialization, transient fields and `serialVersionUID`.
- A5/B2: use a disposable local database to compare unsafe SQL concatenation with PreparedStatement. Do not test against real accounts or production data.
- B1: test normal, missing and damaged IDs; measure lookup for 1,000 through 100,000 rows.
- B3: measure 1,000, 10,000 and 100,000 rows; test auto-commit vs transaction and forced rollback.
- B4: run migration and capture DatabaseMetaData output.

The program provides runnable demonstrations, but actual performance results depend on your computer, disk, JDK, MySQL server and network. Do not copy invented timings into the report.
