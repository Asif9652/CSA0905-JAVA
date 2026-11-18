# Test plan and evidence checklist

Capture terminal screenshots and paste actual measurements into `results/` after running the programs.

## B1 file-store tests

| Test | Input/action | Expected result |
|---|---|---|
| Normal find | Find P1003 in PassStore demo | Six fields returned |
| Missing ID | Find P1002 (damaged line) | `null` |
| Damaged field count | `P2001,ONLY,TWO` | Row skipped |
| Non-numeric amount | `P2005,...,not-a-number` | Row skipped |
| Empty line | Blank line | Row skipped |
| Route listing | List route 7 | Only valid route-7 records |

Run `PassStore` and `FileBenchmarks`. Record local values in the CSV templates. Run each benchmark at least three times; use median values for the report. Do not compare a single cold run with a warmed run without noting the difference.

## B2 adversarial inputs

Run `JdbcSecurityDemo` against the local schema. Expected: unsafe concatenated SQL returns a row for the crafted ID; PreparedStatement treats the payload as literal data and returns no row. The demo is intentionally vulnerable and must not be deployed.

The sample migration file includes a wrong field count and non-numeric amount; `MigrationTool` should report rejected lines.

## B3 JDBC correctness and performance

Run `JdbcBenchmarks` only on the assignment database. It inserts 10, 1,000, 10,000 and 100,000 rows using Statement, PreparedStatement and transactional batches. Verify output counts and column metadata. Timings depend on the machine, MySQL configuration, indexes, network and JVM warm-up.

For rollback evidence, the benchmark injects a non-numeric route halfway through a transaction; conversion fails and the transaction is rolled back. Confirm the remaining row count is zero.

## B4 migration and metadata

Run:
`java -cp "<classpath>" MigrationTool data/passes.csv`

Expected: valid lines migrate, invalid lines are printed with line numbers. Then run `MetadataInspector` and capture database product, driver version and table names.

## Report discussion prompts

- Explain why a buffered scan has fewer read operations than byte-at-a-time reading.
- Explain why one malformed record should not terminate the entire scan.
- Compare O(n) scan lookup with a HashMap index (O(n) build + expected O(1) per lookup, O(n) memory).
- Compare Statement, PreparedStatement and batch execution using your actual measured values.
- Explain the difference between auto-commit and a single transaction when a failure occurs.
- Recommend a relational database for multi-campus concurrent issuance, with indexes, transactions, constraints, backups and access control.
