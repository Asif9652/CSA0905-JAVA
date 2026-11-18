# CSA09 – Programming in Java – Assignment 5 (CO5)

**Student:** K. Abu Hujaifa  
**Register number:** 192472308  
**Course:** CSA09 – Programming in Java  
**Assignment:** 5 – Data Persistence Engineering: Files and Databases  
**Java:** Java 17+ recommended (the code uses modern `Files` APIs; Java 17 is a convenient baseline)

> Replace the student details if required by your faculty. Add your GitHub repository URL and your own execution screenshots and measured results before submission.

## Project structure

- `PartA/` – written design answers, pseudocode and complexity analysis for A1–A5.
- `PartB/src/` – complete Java implementations for B1–B4 and supporting demonstrations.
- `PartB/sql/` – MySQL database/table setup.
- `PartB/data/` – sample CSV input.
- `PartB/results/` – CSV templates for your actual benchmark results.

## Requirements

1. JDK 17 or later; verify with `java -version` and `javac -version`.
2. MySQL Server 8.x (or compatible MySQL/MariaDB server).
3. MySQL Connector/J JAR for JDBC tasks. Download it from the official MySQL Connector/J page and place the JAR in `PartB/lib/`.
4. Create the database by running `PartB/sql/schema.sql`.
5. Set environment variables:
   - Windows PowerShell:
     ```powershell
     $env:DB_URL="jdbc:mysql://localhost:3306/csa09_assignment5"
     $env:DB_USER="root"
     $env:DB_PASSWORD="your_password"
     ```
   - macOS/Linux:
     ```bash
     export DB_URL='jdbc:mysql://localhost:3306/csa09_assignment5'
     export DB_USER='root'
     export DB_PASSWORD='your_password'
     ```
   Never commit real credentials to GitHub.

## Compile and run

Open a terminal in `PartB`.

### File-store and file benchmark (no external library)
```bash
javac -d out src/PassStore.java src/FileBenchmarks.java
java -cp out PassStore
java -cp out FileBenchmarks
```

### JDBC programs
On Windows, use `;` as the classpath separator:
```powershell
mkdir out -ErrorAction SilentlyContinue
javac -cp "lib/mysql-connector-j.jar" -d out src/*.java
java -cp "out;lib/mysql-connector-j.jar" JdbcSecurityDemo
java -cp "out;lib/mysql-connector-j.jar" JdbcBenchmarks
java -cp "out;lib/mysql-connector-j.jar" MigrationTool data/passes.csv
java -cp "out;lib/mysql-connector-j.jar" MetadataInspector
```
On macOS/Linux, replace `;` with `:` in the runtime classpath.

If your Connector/J file has a versioned filename, use that exact filename in the classpath.

## Programs included

| File | Purpose |
|---|---|
| `PassStore.java` | Add/find/list bus passes; skips malformed rows and validates values |
| `FileBenchmarks.java` | Byte-at-a-time vs buffered line counting; lookup-growth experiment |
| `PassLoader.java` | TODO-completed transactional JDBC batch loader (1,000-row batches) |
| `JdbcSecurityDemo.java` | Demonstrates unsafe SQL injection versus PreparedStatement |
| `JdbcBenchmarks.java` | Compares Statement, PreparedStatement and batch insertion |
| `MigrationTool.java` | Validates CSV rows and migrates valid rows transactionally |
| `MetadataInspector.java` | Reports database/driver details and tables |
| `WalletTransfer.java` | Atomic wallet transfer with commit/rollback |
| `SerializationDemo.java` | Object graph serialization and transient fields |
| `DaoDesignDemo.java` | Generic DAO interface and illustrative Student DAO |

## Important execution notes

- `PassStore` uses a simple comma-delimited format for the assignment demonstration. The implementation rejects commas/newlines in fields instead of silently corrupting the format. A production CSV implementation should use a standards-compliant CSV parser/escaping format.
- The benchmark program creates its own temporary test files. Results depend on CPU, disk, operating system, JVM warm-up, antivirus and cache state. Run each measurement several times and report your local median/average.
- JDBC programs require a running database, the schema script and Connector/J.
- `JdbcBenchmarks` clears the test table between methods and runs. Do not point it at a production database.
- The injected SQL string in `JdbcSecurityDemo` is intentionally unsafe and must only be used against this local assignment database.
- Screenshots and actual benchmark values are not fabricated or included. Capture your own terminal/database screenshots after execution.

## GitHub submission workflow

Create a repository named `CSA09-192472308-Assignment5`. Add this project, commit progressively (at least one meaningful commit per task), and add your faculty as collaborator if the repository is private. Include the final GitHub URL in your report and README.

Suggested commits:
1. `Add A1-A2 persistence and buffered I/O design`
2. `Add A3-A5 DAO serialization and transaction design`
3. `Implement B1 file store and benchmarks`
4. `Implement B2 adversarial JDBC tests`
5. `Implement B3 batch loading and JDBC comparisons`
6. `Implement B4 migration and metadata inspection`
7. `Add execution evidence and final report`
