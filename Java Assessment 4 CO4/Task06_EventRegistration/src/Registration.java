import java.sql.Timestamp;

public class Registration {
    private final int id;
    private final String studentId;
    private final String studentName;
    private final String eventName;
    private final Timestamp registeredAt;

    public Registration(int id, String studentId, String studentName,
                        String eventName, Timestamp registeredAt) {
        this.id = id; this.studentId = studentId; this.studentName = studentName;
        this.eventName = eventName; this.registeredAt = registeredAt;
    }
    public int getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getEventName() { return eventName; }
    public Timestamp getRegisteredAt() { return registeredAt; }
}
