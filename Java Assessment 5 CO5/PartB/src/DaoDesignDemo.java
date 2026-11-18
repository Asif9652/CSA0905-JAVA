import java.sql.*;
import java.util.*;

interface Dao<T,K> {
    int insert(T entity) throws SQLException;
    Optional<T> findById(K id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(K id) throws SQLException;
}
class Student {
    final int id; final String name,email;
    Student(int id,String name,String email){this.id=id;this.name=name;this.email=email;}
}
class StudentDao implements Dao<Student,Integer> {
    private final Connection con;
    StudentDao(Connection con){this.con=con;}
    public int insert(Student s)throws SQLException{
        try(PreparedStatement p=con.prepareStatement("INSERT INTO student(id,name,email) VALUES(?,?,?)")){
            p.setInt(1,s.id);p.setString(2,s.name);p.setString(3,s.email);return p.executeUpdate();
        }
    }
    public Optional<Student> findById(Integer id)throws SQLException{
        try(PreparedStatement p=con.prepareStatement("SELECT id,name,email FROM student WHERE id=?")){
            p.setInt(1,id);try(ResultSet r=p.executeQuery()){
                return r.next()?Optional.of(new Student(r.getInt(1),r.getString(2),r.getString(3))):Optional.empty();
            }
        }
    }
    public List<Student> findAll()throws SQLException{
        List<Student> out=new ArrayList<>();
        try(Statement s=con.createStatement();ResultSet r=s.executeQuery("SELECT id,name,email FROM student")){
            while(r.next())out.add(new Student(r.getInt(1),r.getString(2),r.getString(3)));
        }return out;
    }
    public boolean update(Student s)throws SQLException{
        try(PreparedStatement p=con.prepareStatement("UPDATE student SET name=?,email=? WHERE id=?")){
            p.setString(1,s.name);p.setString(2,s.email);p.setInt(3,s.id);return p.executeUpdate()==1;
        }
    }
    public boolean delete(Integer id)throws SQLException{
        try(PreparedStatement p=con.prepareStatement("DELETE FROM student WHERE id=?")){
            p.setInt(1,id);return p.executeUpdate()==1;
        }
    }
}
public class DaoDesignDemo {
    public static void main(String[] args) {
        System.out.println("StudentDao implements generic Dao<Student, Integer>.");
        System.out.println("Run the DAO methods after creating the student table and supplying a JDBC connection.");
    }
}
