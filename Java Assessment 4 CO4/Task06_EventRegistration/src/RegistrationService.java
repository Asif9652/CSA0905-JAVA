import java.sql.SQLException;
import java.util.List;

public class RegistrationService {
    private final RegistrationDao dao = new RegistrationDao();

    public void register(String studentId, String studentName, int eventId) throws SQLException {
        if (studentId == null || !studentId.matches("[A-Za-z0-9_-]{3,30}"))
            throw new IllegalArgumentException("Student ID must be 3–30 letters, digits, _ or -.");
        if (studentName == null || studentName.isBlank())
            throw new IllegalArgumentException("Student name is required.");
        if (eventId <= 0) throw new IllegalArgumentException("Choose an event.");
        try {
            dao.register(studentId.trim(), studentName.trim(), eventId);
        } catch (SQLException e) {
            // MySQL duplicate key error 1062: database constraint is the final concurrency-safe guard.
            if (e.getErrorCode() == 1062)
                throw new IllegalArgumentException("This student is already registered for this event.");
            throw e;
        }
    }

    public List<Registration> page(Integer eventId, int page, int pageSize) throws SQLException {
        if (page < 0 || pageSize < 1 || pageSize > 200)
            throw new IllegalArgumentException("Invalid page request.");
        return dao.findPage(eventId, pageSize, page * pageSize);
    }

    public List<String[]> export(Integer eventId) throws SQLException {
        return dao.exportAll(eventId);
    }
}
