import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    // Configure for your local MySQL/MariaDB installation. Do not commit real credentials.
    private static final String URL = "jdbc:mysql://localhost:3307/campus_events";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Db() {}
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
