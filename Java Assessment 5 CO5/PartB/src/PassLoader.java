import java.sql.*;
import java.util.List;

/** Completed TODOs: typed parameters, 1,000-row batches, commit and rollback. */
public class PassLoader {
    public static int loadAll(Connection con, List<String[]> passes) throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, validity_date, amount) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int n = 0;
            int pending = 0;
            for (String[] p : passes) {
                if (p.length != 6) throw new SQLException("Expected six fields");
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setDate(5, Date.valueOf(p[4]));
                ps.setDouble(6, Double.parseDouble(p[5]));
                ps.addBatch();
                n++;
                pending++;
                if (pending == 1000) {
                    ps.executeBatch();
                    pending = 0;
                }
            }
            if (pending > 0) ps.executeBatch();
            con.commit();
            return n;
        } catch (SQLException | RuntimeException e) {
            try { con.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }
}
