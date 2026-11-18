# Task 5 – Four event-handling styles

| Style | Implementation | Access to frame fields | Reuse/change |
|---|---|---|---|
| Frame implements ActionListener | Frame implements `actionPerformed` | Direct | Handler is coupled to frame; one class can become crowded |
| Named inner class | `ConvertHandler implements ActionListener` | Direct access to enclosing instance fields | Named and reusable within the enclosing class |
| Anonymous inner class | Inline `new ActionListener(){...}` | Direct | Good for one-off handlers; verbose |
| Lambda | `e -> convert()` | Captures enclosing instance | Concise for a single-method interface; easiest to read for short handlers |

All four implementations register the same handler for the button and Enter key in the text field, and show a dialog for success or invalid input. The supplied lambda TODO implementation is `ConverterLambda.java`.

For a large application, a named handler or shared Swing `Action` is often preferable when behaviour is reused across buttons, menus and keyboard shortcuts. A method reference such as `this::convert` is concise when the target method signature matches the listener method.

## Alternative: shared Action

Create one `AbstractAction` whose `actionPerformed` calls `convert()`. Attach it to a JButton, JMenuItem and an InputMap/ActionMap keyboard shortcut. The same action object centralises enable/disable state and label/icon. It takes O(1) action object space for the shared operation, plus component references. Separate anonymous listeners may create O(t) listener objects for t triggers. Adding a trigger to a shared Action requires registering the existing object; dispatch work is O(1) per event.
