# Task 2 – Hospital outpatient appointment desk

## 2.1 Event model

| Event source | Event object | Listener interface | Action |
|---|---|---|---|
| Patient ID text field | ActionEvent | ActionListener | Validate ID / move focus |
| Department combo box | ActionEvent | ActionListener | Refresh doctor list |
| Doctor combo box | ActionEvent | ActionListener | Refresh or clear time slots |
| Time-slot combo box | ActionEvent | ActionListener | Store selected slot |
| Book button | ActionEvent | ActionListener | Validate and book appointment |
| Window close control | WindowEvent | WindowListener / WindowAdapter | Confirm if booking is in progress, then close |
| Cancel button | ActionEvent | ActionListener | Clear/cancel form |

## 2.2 Component hierarchy

```text
JFrame: AppointmentDeskFrame [BorderLayout]
├── Header JPanel [BorderLayout.NORTH]
│   └── JLabel: Appointment Desk
├── FormPanel JPanel [GridBagLayout.CENTER]
│   ├── JLabel + JTextField: Patient ID
│   ├── JLabel + JComboBox: Department
│   ├── JLabel + JComboBox: Doctor
│   ├── JLabel + JComboBox: Time slot
│   └── JButton: Book appointment
└── StatusPanel JPanel [BorderLayout.SOUTH]
    └── JLabel: confirmation/error
```

## 2.3 Handler pseudocode

```text
ALGORITHM RefreshDoctors
INPUT selectedDepartment, doctorsByDepartment
BEGIN
    doctorCombo.removeAllItems()
    IF selectedDepartment is null THEN RETURN
    doctorList ← doctorsByDepartment.get(selectedDepartment)
    FOR EACH doctor IN doctorList DO
        doctorCombo.addItem(doctor)
    END FOR
    IF doctorList is empty THEN
        statusLabel.setText("No doctors available")
    END IF
END

ALGORITHM BookAppointment
INPUT patientId, department, doctor, timeSlot
BEGIN
    IF patientId is empty THEN SHOW "Enter patient ID"; RETURN
    IF department OR doctor OR timeSlot is not selected THEN
        SHOW "Select department, doctor and time"; RETURN
    END IF
    IF bookingInProgress THEN RETURN
    bookingInProgress ← true
    DISABLE Book button
    CALL appointment service (background task if database/network is used)
    IF booking succeeds THEN SHOW confirmation
    ELSE SHOW error
    bookingInProgress ← false
    ENABLE Book button
END
```

## 2.4 Event trace
1. `main` schedules UI creation using `SwingUtilities.invokeLater`.
2. Constructor creates frame, panels, controls, data map and listeners.
3. `setVisible(true)` displays the window.
4. Department selection fires `ActionEvent`; its listener refreshes doctors.
5. Doctor and time selections update the form state.
6. Book fires `ActionEvent`; validation runs and booking service is called.
7. On close, `windowClosing(WindowEvent)` asks for confirmation if booking is active; confirmed close calls `dispose()`.
8. `windowClosed` may occur after disposal.

An applet uses container lifecycle callbacks `init()`, `start()`, `paint()`, `stop()` and `destroy()`. A desktop JFrame is not an applet: it uses construction, visibility, event callbacks, window events and `dispose()`.

## Alternative and complexity
A preloaded `Map<String,List<Doctor>>` gives O(1) average department lookup, plus O(m_d) to populate a combo with m_d doctors. Memory is O(d+m) for d departments and m doctors. Querying the database on each change adds network/database latency and query work (roughly O(log m + m_d) for an indexed query and returned rows), but can use less local memory if only the current department is loaded. Database queries should run asynchronously.
