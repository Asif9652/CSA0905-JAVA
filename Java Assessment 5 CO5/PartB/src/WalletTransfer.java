import java.sql.*;

/** Assumes wallet(wallet_id VARCHAR PRIMARY KEY, balance DECIMAL(12,2)). */
public class WalletTransfer {
    public static void transfer(Connection c,String from,String to,java.math.BigDecimal amount) throws SQLException {
        if(amount.signum()<=0) throw new IllegalArgumentException("Amount must be positive");
        boolean old=c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            try(PreparedStatement debit=c.prepareStatement(
                    "UPDATE wallet SET balance=balance-? WHERE wallet_id=? AND balance>=?")) {
                debit.setBigDecimal(1,amount); debit.setString(2,from); debit.setBigDecimal(3,amount);
                if(debit.executeUpdate()!=1) throw new SQLException("Source missing or insufficient balance");
            }
            try(PreparedStatement credit=c.prepareStatement(
                    "UPDATE wallet SET balance=balance+? WHERE wallet_id=?")) {
                credit.setBigDecimal(1,amount); credit.setString(2,to);
                if(credit.executeUpdate()!=1) throw new SQLException("Destination wallet not found");
            }
            c.commit();
        } catch(SQLException|RuntimeException e) {
            c.rollback(); throw e;
        } finally { c.setAutoCommit(old); }
    }
}
