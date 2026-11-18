import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.sql.*;

/** Migrates validated six-column CSV rows; bad rows are reported with line numbers. */
public class MigrationTool {
    public static void main(String[] args) throws Exception {
        Path input = args.length > 0 ? Paths.get(args[0]) : Paths.get("data/passes.csv");
        List<String[]> valid = new ArrayList<>();
        int lineNo=0, rejected=0;
        try(BufferedReader r=Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            while((line=r.readLine())!=null) {
                lineNo++;
                String[] p=PassStore.parseValid(line);
                if(p==null) {
                    rejected++;
                    System.err.println("Rejected line "+lineNo+": "+line);
                } else {
                    try { java.sql.Date.valueOf(p[4]); }
                    catch(IllegalArgumentException ex) {
                        rejected++; System.err.println("Rejected line "+lineNo+": invalid date"); continue;
                    }
                    valid.add(p);
                }
            }
        }
        try(Connection c=Db.connect()) {
            int inserted=PassLoader.loadAll(c,valid);
            System.out.println("Valid rows inserted: "+inserted);
            System.out.println("Rejected rows: "+rejected);
        }
    }
}
