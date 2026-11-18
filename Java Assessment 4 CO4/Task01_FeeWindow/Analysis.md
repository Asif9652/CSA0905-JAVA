# Task 1 – GUI design review

## 1.1 Deficiencies
1. UI and payment logic are mixed in one listener, making testing and maintenance difficult.
2. `==` compares String references; use `.equals()` or separate listeners/actions.
3. No validation for empty registration, non-numeric, zero, negative, NaN or infinite amounts.
4. JDBC work runs on the event-dispatch thread (EDT), freezing the window.
5. `Thread.sleep(3000)` on the EDT prevents repainting and user interaction.
6. `System.out.println` gives no visible user feedback.
7. Receipt button is not implemented; it falls into the clear branch.
8. SQL concatenation permits SQL injection; use PreparedStatement.
9. Connection and statement are not closed; use try-with-resources.
10. Window close behaviour is not handled.
11. Checked exceptions from JDBC and sleep are not handled.

## 1.2 Repaired class structure
- `FeeWindow`: collects input, attaches listeners, displays results.
- `FeeService`: validates input and owns payment rules.
- `PaymentResult`: returns success/failure, message and receipt identifier.

The supplied program simulates payment. A real payment service should call the gateway/JDBC from `SwingWorker` or another background executor, use prepared SQL, and close JDBC resources safely.

## 1.3 Design defence (under 100 words)
The redesigned window only handles user interaction and presentation, while FeeService owns validation and payment rules. FeeService can be tested without creating a GUI, and the UI can change without rewriting the payment logic. Separate handlers give each button one responsibility. PaymentResult provides a consistent success or failure response. Real network and database work should run in SwingWorker so the event-dispatch thread remains responsive.

## Alternative and complexity
A single action-command chain may perform up to k comparisons for k buttons, O(k) dispatch selection. Separate listeners or Swing Actions are directly attached to controls, giving O(1) handler selection in the usual event-dispatch model. k listener objects require O(k) additional object/reference space. A shared Swing Action is useful when one operation is triggered by a button, menu item and keyboard shortcut.
