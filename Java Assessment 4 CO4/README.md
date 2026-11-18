# SIMATS Engineering – Programming in Java – Assessment 4 (CO4)

**Student:** K. Abu Hujaifa  
**Register number:** 192472308  
**Course:** CSA09 – Programming in Java  
**Assessment:** CO4 – Assessment 4, Event-Driven GUI Design  
**JDK verified:** OpenJDK 21.0.13 (Temurin)

## Tasks and implementation

| Task | Implementation |
|---|---|
| 01 – Fee Window | Swing fee form, input validation/service/result separation, payment feedback, receipt display and close confirmation. Payment is simulated. |
| 02 – Appointment Desk | Swing patient form, department-driven doctors and slots, booking validation/confirmation, cancel action and close confirmation. Data is demonstration-only. |
| 03 – Library Search | Resizable Swing search screen with input validation, category and title search over a small local demonstration catalog, and reserve feedback. |
| 04 – DrawPad | AWT drawing pad with corrected `mouseClicked` override and persistent click marks across repaint. |
| 05 – Currency Converter | Four event-listener examples (frame listener, named inner class, anonymous class and lambda); demonstration INR rates are shown in each window. |
| 06 – Event Registration | Swing/JDBC registration with service/DAO separation, parameterized SQL, duplicate protection, background work, filtering, paging and CSV export. Requires a configured database. |

The task design/analysis documents remain in their corresponding task folders. Task 2 uses an in-memory demonstration appointment list; Task 3 uses a local demonstration catalog; Task 1 and Task 5 do not contact payment or exchange-rate services.

## Compile independently (PowerShell)

Run from the assessment root. Each task writes class files to its own `out` directory:

```powershell
$root = (Get-Location).Path
$tasks = @(
  @{ Dir = 'Task01_FeeWindow'; Source = '*.java' },
  @{ Dir = 'Task02_AppointmentDesk'; Source = '*.java' },
  @{ Dir = 'Task03_LibrarySearch'; Source = '*.java' },
  @{ Dir = 'Task04_DrawPad'; Source = '*.java' },
  @{ Dir = 'Task05_CurrencyConverter'; Source = '*.java' },
  @{ Dir = 'Task06_EventRegistration'; Source = 'src\*.java' }
)
foreach ($task in $tasks) {
  Push-Location (Join-Path $root $task.Dir)
  New-Item -ItemType Directory -Force out | Out-Null
  javac -encoding UTF-8 -d out (Get-ChildItem $task.Source -File).FullName
  if ($LASTEXITCODE -ne 0) { throw "Compilation failed: $($task.Dir)" }
  Pop-Location
}
```

Compilation was completed successfully for all six tasks with JDK 21. This confirms compilation only; it is not a substitute for recording GUI/database test observations.

Additional direct checks passed:
- Task 1 `FeeService`: a positive amount succeeded; empty, non-numeric, zero and infinite amounts were rejected.
- Task 6 `RegistrationService`: invalid student ID, blank name and invalid event ID were rejected before any database connection was attempted.
- Task 2, Task 3 and Task 5's lambda window were displayed during this session. The short-lived debugger windows did not remain targetable long enough to verify input interactions or save screenshot files. Task 6 was not runtime-tested because its JDBC driver is absent.

## Run applications (PowerShell)

After compilation, run each command from the assessment root:

```powershell
java -cp .\Task01_FeeWindow\out FeeWindow
java -cp .\Task02_AppointmentDesk\out AppointmentDesk
java -cp .\Task03_LibrarySearch\out LibrarySearchScreen
java -cp .\Task04_DrawPad\out DrawPad
java -cp .\Task05_CurrencyConverter\out ConverterLambda
```

To run the other Task 5 listener examples, use `ConverterFrameListener`, `ConverterInnerClass`, or `ConverterAnonymous` in place of `ConverterLambda`.

## Task 6 – MySQL/MariaDB setup and run

1. Start a local MySQL/MariaDB server and execute `Task06_EventRegistration/schema.sql`.
2. Add MySQL Connector/J or a compatible MariaDB JDBC driver JAR to `Task06_EventRegistration\lib\`.
3. Configure the local URL, user and password in `Task06_EventRegistration\src\Db.java`; do not commit real credentials.
4. From the assessment root:

```powershell
javac -cp ".\Task06_EventRegistration\lib\*" -d .\Task06_EventRegistration\out .\Task06_EventRegistration\src\*.java
java -cp ".\Task06_EventRegistration\out;.\Task06_EventRegistration\lib\*" EventRegistrationApp
```

Task 6 compiled, but its runtime requires the external JDBC driver and configured database, neither of which is included in `lib` at present.

## Screenshots and test record

Each task has a `screenshots` folder; currently no image files are saved there. To capture evidence on Windows, run the command above, press **Win+Shift+S**, choose rectangular snip, drag around the app window, open the Snipping Tool save prompt and save a PNG in that task's `screenshots` folder. Suggested evidence:

- Task 1: `main-window.png`, `validation-error.png`, `receipt.png`
- Task 2: `main-window.png`, `validation-error.png`, `booking-confirmation.png`
- Task 3: `small.png`, `medium.png`, `large.png`
- Task 4: `drawing.png`, `restored-after-minimize.png`
- Task 5: one valid and one invalid-input capture for each converter style
- Task 6: normal form, validation error, duplicate result, filtered/paged table and CSV export

Only record observations actually made in `REPORT/ExperimentLog.md`. Task 6's scenarios require the configured JDBC driver and database.
