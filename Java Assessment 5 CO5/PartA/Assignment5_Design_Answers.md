# PART A – PERSISTENCE DESIGN & PSEUDOCODE

## A1 – Design a File Format (7 marks)

### A1.1 Record representation

**Delimited text format:** one pass per line, fields in this order:

`passId,regNo,name,routeNo,validityDate,amountPaid`

Example:
`P1001,192311001,Arun,12,2027-03-31,4500.00`

Use UTF-8. A delimiter inside a name must be escaped according to a defined CSV convention (for example, quote the field and double embedded quotes), or reject it if implementing a deliberately simple format. Never split an unescaped free-form name on commas. The Part B teaching implementation rejects commas, quotes and line breaks so the simple five-field format remains unambiguous.

**Binary DataOutputStream format:** fields in the same logical order:
- pass ID: `writeUTF` (modified UTF-8 with a length prefix)
- register number: `writeUTF`
- name: `writeUTF`
- route number: `writeInt` (4 bytes)
- validity date: epoch day as `writeLong` (8 bytes)
- amount paid: `writeDouble` (8 bytes)

`writeUTF` stores a length prefix, so string fields are variable-length rather than fixed-size. Validate maximum string lengths before writing. A fixed-width binary layout would instead reserve documented byte lengths and pad shorter values.

### A1.2 Operations – text file

All operations use `BufferedReader`/`BufferedWriter` over UTF-8 readers/writers. Buffering reduces expensive low-level reads/writes. `FileReader`/`FileWriter` use the platform default charset unless configured indirectly; prefer `Files.newBufferedReader/newBufferedWriter` with UTF-8. `FileInputStream/FileOutputStream` are appropriate for raw bytes, not parsing text records.

**ADD(pass):**
1. Validate required fields, numeric values, date and delimiter restrictions.
2. Open file with create + append using a buffered UTF-8 writer.
3. Write the encoded record and a newline.
4. Flush/close using try-with-resources.
5. For stronger crash safety, write a complete temporary file and atomically replace the original, or use a database transaction. Appending a line alone is not a full durability protocol.

**FIND(passId):**
1. Open a buffered UTF-8 reader.
2. Read one line at a time until EOF.
3. Parse the line; if field count or field values are invalid, record/skip the damaged line.
4. If the first field equals passId, return the parsed record.
5. Return NOT_FOUND after EOF.

**LIST_BY_ROUTE(route):**
1. Open a buffered UTF-8 reader.
2. Read each line and validate it.
3. Skip malformed rows.
4. Parse route number and append matching records to the result.
5. Return all matches.

### A1.3 Damaged data

- **Damaged line:** validate exact field count, required non-empty values, date format, integer route and decimal amount. Skip or quarantine the bad line and report its line number; do not terminate the whole scan.
- **Missing file:** distinguish a missing file from an empty file. For a first-time store, create it when adding; for a read operation, return a clear “store not found” status rather than silently claiming there are no records.
- **Incomplete write/power failure:** a final line may be truncated. Detect it through field/type validation and skip/quarantine it. Stronger designs use a temporary file + atomic rename, checksums/record lengths, journaling, or a transactional database. Flush is not equivalent to guaranteed physical durability.

### Alternative: RandomAccessFile

With fixed-size records, record `i` is at offset `i × recordSize`; seeking to a known index is O(1). But finding by pass ID still requires O(n) scanning unless a separate index maps ID to record number. Fixed-size name/ID fields waste space when actual values are shorter and impose maximum lengths. Text scan lookup is O(n), append is amortized O(1), route listing is O(n).

## A2 – Don't Read What You Don't Need (5 marks)

### A2.1 Predict the cost

A 50 MiB file contains 52,428,800 bytes. A loop calling `FileInputStream.read()` once per byte makes 52,428,800 Java read calls. Depending on implementation and buffering below the Java layer, this can cause a very large number of method/native calls and poor throughput. Counting newline bytes is O(N) time and O(1) extra space.

### A2.2 Buffered redesign

Read a byte array, for example 64 KiB, repeatedly. For each returned byte, increment the line count when it equals `'\n'`. The loop makes approximately `ceil(fileSize / 65536)` read calls rather than one per byte. `BufferedInputStream` achieves a similar reduction by fetching blocks into an internal buffer.

### A2.3 When buffering gives little benefit

If the source already supplies large blocks (for example, an existing in-memory byte array) or the program performs only one tiny read, another buffer may add copying and allocation without reducing meaningful I/O overhead. If the bottleneck is CPU parsing or remote latency, buffering may not solve it.

### Alternative: Files.readAllLines

`Files.readAllLines` reads and stores all lines in memory: O(N) time and O(N) memory, with additional object overhead per line. A buffered streaming scan is O(N) time and O(B) memory, where B is the buffer size. For a 50 MiB log, streaming is safer when memory is constrained or only counts are needed. `readAllLines` is convenient when the complete set of lines is small enough to fit comfortably in memory and repeated random processing is required.

## A3 – Discover the Data-Access Design Pattern (5 marks)

### A3.1 Repeated pattern

Common steps: obtain connection, prepare SQL, bind values, execute, iterate ResultSet, map rows to objects, handle SQL exceptions, close resources. Differences: entity/table name, columns, key type, SQL statements and row-to-object mapping.

### A3.2 Generic DAO

```java
public interface Dao<T, K> {
    int insert(T entity) throws SQLException;
    Optional<T> findById(K id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(K id) throws SQLException;
}
```

A `StudentDao implements Dao<Student, Integer>` contains student-specific SQL and mapping. A shared connection provider and common resource/error-handling conventions remove duplicated infrastructure.

### A3.3 Object-table mapping

`Student(id, name, email)` maps to table `student(id INT PRIMARY KEY, name VARCHAR(...), email VARCHAR(...))`. `Student.id` maps to `student.id` and is the primary key. Use PreparedStatement parameters for values.

### Alternative: SQL in service/GUI

If SQL is embedded in each service or GUI class, a renamed column may require edits in many classes, increasing maintenance risk. A DAO centralizes table-specific SQL, so normally one DAO needs modification. Both designs have broadly the same database operation time and result memory complexity: network/database work dominates; DAO abstraction itself adds negligible constant overhead. The DAO improves separation and maintainability, not SQL asymptotic complexity.

## A4 – What Should Be Serialized? (4 marks)

### A4.1 Persisted fields

Persist durable domain state: hostel data, rooms, residents, names and register numbers. Mark the live `Connection` transient because it is an external resource, not serializable durable state; reconnect after deserialization. Passwords should not be stored in plaintext in serialized files. Prefer not to serialize credentials at all; store salted password hashes in a protected database. If a credential-like field must be excluded, mark it transient.

Serializing `Hostel` traverses reachable non-transient serializable objects: its room list, each Room, and each Resident. Every reachable object must implement `Serializable`; shared references are preserved within the object stream. Transient fields are not restored as live values.

### A4.2 Version changes

If `phoneNumber` is added and an explicit compatible `serialVersionUID` is retained, an older stream can generally be read; the new field receives its default value (`null` for String). Application code must handle missing values. If the computed UID changes, deserialization can throw `InvalidClassException`. An explicit UID supports controlled compatible evolution but does not automatically migrate semantic changes.

### Alternative: CSV/JSON

Text formats are portable, inspectable and easier to evolve with optional fields, but require explicit parsing/mapping and escaping. Java serialization is convenient for object graphs but is Java-specific and sensitive to class evolution/security concerns. Both save and load n residents in O(n) time and O(n) storage for the complete representation; serialization may be more compact or faster for object graphs but this depends on data and implementation. CSV can be compact; JSON is more verbose but self-describing. Adding a field requires a schema/version/default policy in either format.

## A5 – Design for Correctness and Safety (4 marks)

### A5.1 Transaction design

```text
ALGORITHM TransferWallet(sourceId, destinationId, amount)
    CONNECT to database
    SET autoCommit to FALSE
    TRY
        UPDATE wallet SET balance = balance - amount
            WHERE wallet_id = sourceId AND balance >= amount
        IF affected rows != 1 THEN
            RAISE insufficient funds or missing source
        END IF

        UPDATE wallet SET balance = balance + amount
            WHERE wallet_id = destinationId
        IF affected rows != 1 THEN
            RAISE missing destination
        END IF

        COMMIT
    CATCH error
        ROLLBACK
        REPORT failure
    FINALLY
        RESTORE autoCommit and close resources
    END TRY
END ALGORITHM
```

The transaction makes both updates one atomic unit. If the second update fails, rollback undoes the first. Production code should also validate positive amount, lock/read balances safely, and use appropriate isolation.

### A5.2 SQL injection

Concatenating input into SQL lets input change SQL syntax. For example, an ID such as `' OR '1'='1' -- ` can make a predicate true and comment out the remainder depending on query construction. Use:
```java
String sql = "SELECT id FROM users WHERE id = ? AND pwd_hash = ?";
try (PreparedStatement ps = con.prepareStatement(sql)) {
    ps.setString(1, id);
    ps.setString(2, passwordHash);
    try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
    }
}
```
PreparedStatement separates SQL structure from values. Real applications must hash passwords using a password-hashing algorithm (Argon2id/bcrypt/scrypt), not compare plaintext.

### Alternative: auto-commit updates

With two independent auto-commit updates, the debit can commit before a failure prevents the credit. Money is lost. A transaction adds coordination/logging overhead, but provides atomicity and correctness. The two approaches are both constant-count database operations, but transaction commit/rollback has additional fixed overhead and avoids inconsistent state.
