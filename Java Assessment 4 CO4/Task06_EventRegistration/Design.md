# Task 6 – Campus event registration design challenge

## Stage 1 – Requirements

**Users:** student (register), committee member (view/filter/export), database administrator (maintain event records).

**Data:** student ID, student name, event ID/name, registration ID and timestamp.

**Testable rules:**
1. A student must provide a valid ID, non-empty name and one event.
2. A student may register only once for the same event.
3. Duplicate prevention must be enforced in the database with `UNIQUE(student_id,event_id)`, not only in the UI.
4. Committee users can filter by event and view registration rows.
5. Rows are loaded in pages of 25 using SQL `LIMIT` and `OFFSET`.
6. JDBC work runs in `SwingWorker`, not on the Swing event-dispatch thread.
7. Export creates a CSV file containing the selected filter's registrations.

## Stage 2 – Screen and event flow

```text
JFrame: EventRegistrationApp
├── JMenuBar
│   └── File → Refresh / Exit
├── Registration JPanel
│   ├── Student ID JTextField
│   ├── Student Name JTextField
│   ├── Event JComboBox
│   └── Register JButton
├── Filter JPanel
│   ├── Event filter JComboBox
│   ├── Apply filter / Previous / Next / Export CSV buttons
├── JTable inside JScrollPane
└── Status JLabel
```

Flow: open app → load event choices asynchronously → enter student data → Register → validate in service → insert using JDBC → unique constraint rejects duplicate → show success/error → refresh first page. Filter changes load the chosen event page. Export fetches matching rows in background and writes CSV.

## Stage 3 – Critical decisions

- UI validates basic format and displays errors.
- `RegistrationService` owns business validation and maps duplicate-key SQL error 1062 to a friendly message.
- `RegistrationDao` owns parameterised SQL and closes JDBC resources.
- The database unique constraint is the concurrency-safe duplicate guard.
- `SwingWorker.doInBackground()` performs database operations; `done()` updates Swing components on the EDT.

## Stage 4 – Class layers and handler pseudocode

```text
EventRegistrationApp (Swing UI)
          |
          v
RegistrationService (validation / application rules)
          |
          v
RegistrationDao (JDBC / SQL)
          |
          v
MySQL: events, registrations
```

Register action:
```text
READ studentId, studentName, selectedEvent
VALIDATE studentId format, name not blank, event selected
DISABLE Register button
START SwingWorker:
    CALL service.register(studentId, studentName, eventId)
ON DONE:
    ENABLE Register button
    IF success THEN show confirmation and refresh table
    ELSE show clear error
```

Filter action:
```text
READ selected event filter
SET page = 0
START SwingWorker:
    rows = service.page(eventId or null, page, pageSize)
ON DONE:
    REPLACE JTable model rows
    SHOW page and row count
```

## Stage 5 – Implementation and tests

The Java implementation is in `src/`. Run `schema.sql`, add MySQL Connector/J to `lib/`, configure `Db.java`, then compile/run as shown in the root README.

Test normal registration, invalid/empty values, duplicate student/event, filtering, previous/next paging and CSV export. Capture actual screenshots.

## Alternative: load all 100,000 rows at once vs pagination

Loading all N=100,000 records has O(N) query/result processing time and O(N) client memory for row objects plus JTable model data. It can cause long pauses and UI memory pressure, especially if performed on the EDT. Paging with page size p loads O(p) rows per request and uses O(p) client-side row memory; total work across all pages is still O(N), but each interaction is bounded by p rows and runs in SwingWorker so the UI remains responsive. OFFSET pagination can become slower for large offsets; keyset pagination is a further improvement.
