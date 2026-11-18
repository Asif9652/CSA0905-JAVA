import java.sql.*;

/** Run only against the local assignment database. Unsafe query is intentionally vulnerable. */
public class JdbcSecurityDemo {
    public static void main(String[] args) throws Exception {
        try (Connection con = Db.connect(); Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS demo_users (" +
                    "id VARCHAR(80) PRIMARY KEY, pwd VARCHAR(80) NOT NULL)");
            st.executeUpdate("DELETE FROM demo_users");
            st.executeUpdate("INSERT INTO demo_users(id,pwd) VALUES ('student1','correct-password')");
            String attackId = "' OR '1'='1' -- ";
            String fakePassword = "wrong";
            String unsafeSql = "SELECT * FROM demo_users WHERE id='" + attackId +
                    "' AND pwd='" + fakePassword + "'";
            try (ResultSet rs = st.executeQuery(unsafeSql)) {
                System.out.println("Unsafe version accepted injected input: " + rs.next());
            }

            String safeSql = "SELECT * FROM demo_users WHERE id=? AND pwd=?";
            try (PreparedStatement ps = con.prepareStatement(safeSql)) {
                ps.setString(1, attackId);
                ps.setString(2, fakePassword);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("PreparedStatement accepted injected input: " + rs.next());
                }
            }
        }
    }
}
