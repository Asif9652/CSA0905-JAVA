import java.sql.
import java.util.*;

/** Benchmark on a disposable assignment table only. */
public class JdbcBenchmarks {
    static void clear(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) { s.executeUpdate("DELETE FROM bus_pass"); }
    }
    static String insertSql = "INSERT INTO bus_pass(pass_id,reg_no,name,route_no,validity_date,amount) VALUES ";
    static String values = "(%s,%s,%s,%d,'%s',%.2f)";
    static String[] row(int i) {
        return new String[]{"JB" + i, "REG" + i, "Test" + i,
                Integer.toString((i % 20) + 1), "2027-12-31", "250.00"};
    }
    static long statement(Connection c, int n) throws SQLException {
        clear(c); long start = System.nanoTime();
        try (Statement s = c.createStatement()) {
            for (int i=0;i<n;i++) {
                String[] p=row(i);
                String sql=insertSql+String.format("('%s','%s','%s',%s,'%s',%s)",
                        p[0],p[1],p[2],p[3],p[4],p[5]);
                s.executeUpdate(sql);
            }
        }
        return (System.nanoTime()-start)/1_000_000;
    }
    static long prepared(Connection c, int n) throws SQLException {
        clear(c); long start=System.nanoTime();
        try (PreparedStatement ps=c.prepareStatement(
                "INSERT INTO bus_pass(pass_id,reg_no,name,route_no,validity_date,amount) VALUES (?,?,?,?,?,?)")) {
            for(int i=0;i<n;i++) {
                String[] p=row(i); bind(ps,p); ps.executeUpdate();
            }
        }
        return (System.nanoTime()-start)/1_000_000;
    }
    static void bind(PreparedStatement ps,String[] p) throws SQLException {
        ps.setString(1,p[0]); ps.setString(2,p[1]); ps.setString(3,p[2]);
        ps.setInt(4,Integer.parseInt(p[3])); ps.setDate(5,java.sql.Date.valueOf(p[4]));
        ps.setBigDecimal(6,new java.math.BigDecimal(p[5]));
    }
    static long batch(Connection c,int n,boolean transaction,boolean forceFailure) throws SQLException {
        clear(c);
        boolean old=c.getAutoCommit();
        c.setAutoCommit(!transaction);
        long start=System.nanoTime();
        String sql="INSERT INTO bus_pass(pass_id,reg_no,name,route_no,validity_date,amount) VALUES (?,?,?,?,?,?)";
        try(PreparedStatement ps=c.prepareStatement(sql)) {
            for(int i=0;i<n;i++) {
                String[] p=row(i);
                if(forceFailure && i==n/2) p[3]="not-an-int";
                bind(ps,p); ps.addBatch();
                if((i+1)%1000==0) ps.executeBatch();
            }
            ps.executeBatch();
            if(transaction) c.commit();
        } catch(Exception e) {
            if(transaction) c.rollback();
            throw e;
        } finally { c.setAutoCommit(old); }
        return (System.nanoTime()-start)/1_000_000;
    }
    static int count(Connection c) throws SQLException {
        try(Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT COUNT(*) FROM bus_pass")) {
            r.next(); return r.getInt(1);
        }
    }
    public static void main(String[] args) throws Exception {
        try(Connection c=Db.connect()) {
            for(int n:new int[]{10,1000,10000,100000}) {
                System.out.printf("%d rows: Statement=%d ms, Prepared=%d ms, Batch(tx)=%d ms%n",
                        n,statement(c,n),prepared(c,n),batch(c,n,true,false));
                try(Statement s=c.createStatement(); ResultSet rs=s.executeQuery(
                        "SELECT COUNT(*) AS rows, MIN(route_no) AS min_route FROM bus_pass")) {
                    rs.next(); System.out.println("Verified rows="+rs.getInt("rows")+
                            ", min_route="+rs.getInt("min_route"));
                }
            }
            try {
                batch(c,100,true,true);
            } catch(Exception expected) {
                System.out.println("Forced invalid row caused failure: "+expected.getMessage());
                System.out.println("Rows remaining after transactional rollback: "+count(c));
            }
            // ResultSetMetaData evidence
            try(Statement s=c.createStatement(); ResultSet rs=s.executeQuery("SELECT * FROM bus_pass LIMIT 1")) {
                ResultSetMetaData md=rs.getMetaData();
                for(int i=1;i<=md.getColumnCount();i++)
                    System.out.println(md.getColumnName(i)+" : "+md.getColumnTypeName(i));
            }
        }
    }
}
