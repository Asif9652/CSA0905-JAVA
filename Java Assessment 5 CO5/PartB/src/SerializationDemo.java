import java.io.*;
import java.util.*;

public class SerializationDemo {
    static class Hostel implements Serializable {
        private static final long serialVersionUID=1L;
        List<Room> rooms=new ArrayList<>();
    }
    static class Room implements Serializable {
        private static final long serialVersionUID=1L;
        List<Resident> residents=new ArrayList<>();
    }
    static class Resident implements Serializable {
        private static final long serialVersionUID=1L;
        String name, registerNumber;
        transient String password; // do not persist a plaintext password
        transient Object liveConnection; // stand-in for a live JDBC Connection
        String phoneNumber; // added later; old stream reads null with compatible UID
        Resident(String n,String r,String p) { name=n; registerNumber=r; password=p; }
    }
    public static void main(String[] args) throws Exception {
        Hostel h=new Hostel(); Room room=new Room();
        room.residents.add(new Resident("Arun","192311001","not-to-be-saved"));
        h.rooms.add(room);
        File f=File.createTempFile("hostel",".ser");
        try {
            try(ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(f))) { out.writeObject(h); }
            try(ObjectInputStream in=new ObjectInputStream(new FileInputStream(f))) {
                Hostel copy=(Hostel)in.readObject();
                Resident r=copy.rooms.get(0).residents.get(0);
                System.out.println(r.name+" / "+r.registerNumber);
                System.out.println("Password after deserialization: "+r.password);
                System.out.println("Phone after old-version-compatible read: "+r.phoneNumber);
            }
        } finally { f.delete(); }
    }
}
