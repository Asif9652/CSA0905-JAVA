import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDao {
    public void register(String studentId, String studentName, int eventId) throws SQLException {
        String sql = "INSERT INTO registrations(student_id, student_name, event_id) VALUES (?, ?, ?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, studentName);
            ps.setInt(3, eventId);
            ps.executeUpdate();
        }
    }

    public List<Registration> findPage(Integer eventId, int limit, int offset) throws SQLException {
        String sql = "SELECT r.registration_id, r.student_id, r.student_name, e.event_name, r.registered_at "
                + "FROM registrations r JOIN events e ON r.event_id=e.event_id "
                + (eventId == null ? "" : "WHERE r.event_id=? ")
                + "ORDER BY r.registration_id DESC LIMIT ? OFFSET ?";
        List<Registration> rows = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (eventId != null) ps.setInt(i++, eventId);
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Registration(rs.getInt("registration_id"),
                            rs.getString("student_id"), rs.getString("student_name"),
                            rs.getString("event_name"), rs.getTimestamp("registered_at")));
                }
            }
        }
        return rows;
    }

    public List<String[]> exportAll(Integer eventId) throws SQLException {
        String sql = "SELECT r.student_id, r.student_name, e.event_name, r.registered_at "
                + "FROM registrations r JOIN events e ON r.event_id=e.event_id "
                + (eventId == null ? "" : "WHERE r.event_id=? ")
                + "ORDER BY r.registration_id DESC";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (eventId != null) ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new String[]{
                        rs.getString("student_id"), rs.getString("student_name"),
                        rs.getString("event_name"), String.valueOf(rs.getTimestamp("registered_at"))
                });
            }
        }
        return rows;
    }
}
