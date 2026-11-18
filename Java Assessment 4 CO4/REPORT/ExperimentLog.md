# Assignment 4 – Experiment log

Student: K. Abu Hujaifa | Register number: 192472308  
JDK: OpenJDK 21.0.13 (Temurin) | OS: Windows | Date: 2026-11-18

Compilation: all six tasks compiled successfully into their independent `out` folders.

Record observations from your own computer; do not guess.

## Task 1 – Fee window
- Valid payment input result: FeeService accepted registration `192472308`, amount `2500` (simulated payment; not a real transaction).
- Empty/invalid input result: Empty, non-numeric, zero and infinite amounts were rejected by FeeService checks.
- Receipt action result: Not UI-tested.

## Task 2 – Appointment desk
- Department-change event and doctor-list refresh design verified: Not interacted with.
- Booking validation scenarios: Not UI-tested.
- Window-close confirmation trace: Not tested. Main window displayed.

## Task 3 – Resize tests

Main window displayed; search and resizing were not interacted with.

| Test | Window dimensions | Results area resized correctly? | Screenshot |
|---|---|---|---|
| Small | Not tested | Not tested | Not captured |
| Medium | Not tested | Not tested | Not captured |
| Large | Not tested | Not tested | Not captured |

## Task 4 – Draw pad
- Number of clicks tested: Not tested
- Dots remained after minimise/restore: Not tested
- Compiler message for misspelled method with @Override: Not reproduced; repaired source compiled successfully.

## Task 5 – Converter styles

| Style | Button works? | Enter works? | Dialog shown? | Code changes observed |
|---|---|---|---|---|
| Frame listener | Debug launch reported started; UI not observed | Not tested | Not tested | Not tested |
| Named inner class | Not launched | Not tested | Not tested | Not tested |
| Anonymous class | Not launched | Not tested | Not tested | Not tested |
| Lambda | Window displayed; conversion not activated | Not tested | Not tested | Not tested |

## Task 6 – Event registration
- Valid registration: Not tested; JDBC driver is absent, database not tested.
- Invalid registration: Service rejected short ID, blank name and invalid event ID before database access.
- Duplicate registration: Not tested against database.
- Filter and pagination: Not tested.
- CSV export path: Not tested.
- UI remained responsive during database operation: Not tested.
