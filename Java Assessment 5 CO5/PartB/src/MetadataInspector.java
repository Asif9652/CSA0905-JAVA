import java.sql.*;

public class MetadataInspector {
    public static void main(String[] args) throws Exception {
        try(Connection c=Db.connect()) {
            DatabaseMetaData md=c.getMetaData();
            System.out.println("Database product: "+md.getDatabaseProductName()+" "+md.getDatabaseProductVersion());
            System.out.println("Driver: "+md.getDriverName()+" "+md.getDriverVersion());
            try(ResultSet rs=md.getTables(c.getCatalog(),null,"%",new String[]{"TABLE"})) {
                System.out.println("Tables:");
                while(rs.next()) System.out.println(" - "+rs.getString("TABLE_NAME"));
            }
        }
    }
}
