
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

// Interface
interface Chargeable {
    double chargingFee(int kWh);
}

// User types
enum UserType {
    STUDENT, STAFF, VISITOR
}

// Parking pass
class ParkingPass {
    private final UserType userType;
    private final boolean valid;

    ParkingPass(UserType userType, boolean valid) {
        this.userType = userType;
        this.valid = valid;
    }

    public boolean isFreeParking() {
        return valid && userType != UserType.VISITOR;
    }

    public boolean isOneDayPass() {
        return valid && userType == UserType.VISITOR;
    }
}

// Abstract Vehicle class
abstract class Vehicle {
    private final String regNo;

    Vehicle(String regNo) {
        if (regNo == null || regNo.isEmpty())
            throw new IllegalArgumentException("Invalid registration number");

        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    public abstract double ratePerHour();

    public double calculateFee(int minutes) {
        if (minutes < 0)
            throw new IllegalArgumentException("Invalid duration");

        int hours = (minutes + 59) / 60;
        return hours * ratePerHour();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vehicle))
            return false;

        Vehicle other = (Vehicle) obj;
        return regNo.equals(other.regNo);
    }

    @Override
    public int hashCode() {
        return regNo.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " - " + regNo;
    }
}

// Two Wheeler
class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    public double ratePerHour() {
        return 10;
    }
}

// Car
class Car extends Vehicle {
    Car(String regNo) {
        super(regNo);
    }

    @Override
    public double ratePerHour() {
        return 30;
    }
}

// Electric Car
class ElectricCar extends Car implements Chargeable {
    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    public double chargingFee(int kWh) {
        return kWh * 12;
    }
}

// E-Bike
class EBike extends TwoWheeler implements Chargeable {
    EBike(String regNo) {
        super(regNo);
    }

    @Override
    public double ratePerHour() {
        return 15;
    }

    @Override
    public double chargingFee(int kWh) {
        return kWh * 5;
    }
}

// Delivery Van
class DeliveryVan extends Vehicle {
    DeliveryVan(String regNo) {
        super(regNo);
    }

    @Override
    public double ratePerHour() {
        return 60;
    }

    // Double car rate between 9 AM and 5 PM
    public double calculateDeliveryFee(LocalDateTime in,
                                       LocalDateTime out) {
        double fee = 0;

        LocalDateTime current = in;

        while (current.isBefore(out)) {
            LocalDateTime next = current.plusHours(1);

            if (next.isAfter(out))
                next = out;

            LocalTime time = current.toLocalTime();

            double rate = (time.isAfter(LocalTime.of(8, 59))
                    && time.isBefore(LocalTime.of(17, 0)))
                    ? 60 : 30;

            double minutes = Duration.between(current, next).toMinutes();
            fee += rate * minutes / 60.0;

            current = next;
        }

        return fee;
    }
}

// Parking Ticket
class ParkingTicket {
    private static int ticketCounter = 1000;

    private final int ticketNo;
    private final Vehicle vehicle;
    private final int floor;
    private final int slot;
    private final LocalDateTime entryTime;
    private final ParkingPass pass;

    ParkingTicket(Vehicle vehicle, int floor, int slot,
                  ParkingPass pass) {
        this.ticketNo = ++ticketCounter;
        this.vehicle = vehicle;
        this.floor = floor;
        this.slot = slot;
        this.pass = pass;
        this.entryTime = LocalDateTime.now();
    }

    public int getTicketNo() {
        return ticketNo;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public int getFloor() {
        return floor;
    }

    public int getSlot() {
        return slot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public ParkingPass getPass() {
        return pass;
    }
}

// Main Parking System
public class ParkingSystem {

    private final Vehicle[][] slots;
    private final boolean[][] chargingSlots;

    private final Map<String, ParkingTicket> activeTickets =
            new HashMap<>();

    private double totalCollection = 0;

    // Constructor
    ParkingSystem(int[] slotsPerFloor) {
        slots = new Vehicle[slotsPerFloor.length][];
        chargingSlots = new boolean[slotsPerFloor.length][];

        for (int f = 0; f < slotsPerFloor.length; f++) {
            slots[f] = new Vehicle[slotsPerFloor[f]];
            chargingSlots[f] = new boolean[slotsPerFloor[f]];

            // Last slot on each floor is a charging slot
            if (slotsPerFloor[f] > 0) {
                chargingSlots[f][slotsPerFloor[f] - 1] = true;
            }
        }
    }

    // Allocate parking slot
    public int[] allocate(Vehicle vehicle) {

        // Prevent duplicate registration
        if (activeTickets.containsKey(vehicle.getRegNo())) {
            System.out.println("Vehicle already parked!");
            return null;
        }

        // EVs and E-bikes prefer charging slots
        if (vehicle instanceof Chargeable) {
            for (int f = 0; f < slots.length; f++) {
                for (int s = 0; s < slots[f].length; s++) {
                    if (slots[f][s] == null && chargingSlots[f][s]) {
                        slots[f][s] = vehicle;
                        return new int[]{f, s};
                    }
                }
            }
        }

        // Search any available slot
        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if (slots[f][s] == null) {
                    slots[f][s] = vehicle;
                    return new int[]{f, s};
                }
            }
        }

        System.out.println("Parking is full!");
        return null;
    }

    // Vehicle entry
    public void vehicleEntry(Vehicle vehicle, ParkingPass pass) {

        int[] position = allocate(vehicle);

        if (position == null)
            return;

        ParkingTicket ticket = new ParkingTicket(
                vehicle, position[0], position[1], pass);

        activeTickets.put(vehicle.getRegNo(), ticket);

        System.out.println("\nVehicle Entered Successfully");
        System.out.println("Vehicle: " + vehicle);
        System.out.println("Ticket Number: " + ticket.getTicketNo());
        System.out.println("Floor: " + position[0]);
        System.out.println("Slot: " + position[1]);
        System.out.println("Entry Time: " + ticket.getEntryTime());
    }

    // Vehicle exit and fee calculation
    public void vehicleExit(String regNo, int chargingUnits) {

        ParkingTicket ticket = activeTickets.get(regNo);

        if (ticket == null) {
            System.out.println("No active parking ticket found!");
            return;
        }

        Vehicle vehicle = ticket.getVehicle();

        LocalDateTime exitTime = LocalDateTime.now();

        long minutes = Duration.between(
                ticket.getEntryTime(), exitTime).toMinutes();

        double fee;

        if (ticket.getPass() != null &&
            (ticket.getPass().isFreeParking() ||
             ticket.getPass().isOneDayPass())) {

            fee = 0;

        } else if (vehicle instanceof DeliveryVan) {

            fee = ((DeliveryVan) vehicle).calculateDeliveryFee(
                    ticket.getEntryTime(), exitTime);

        } else {

            fee = vehicle.calculateFee((int) minutes);
        }

        // Additional charging fee
        if (vehicle instanceof Chargeable) {
            fee += ((Chargeable) vehicle).chargingFee(chargingUnits);
        }

        // Release parking slot
        slots[ticket.getFloor()][ticket.getSlot()] = null;

        activeTickets.remove(regNo);

        totalCollection += fee;

        System.out.println("\nVehicle Exited Successfully");
        System.out.println("Registration Number: " + regNo);
        System.out.println("Parking Duration: " + minutes + " minutes");
        System.out.println("Parking Fee: Rs. " + fee);
        System.out.println("Slot Released Successfully");
    }

    // Display parking status
    public void displayStatus() {

        System.out.println("\n===== PARKING STATUS =====");

        for (int f = 0; f < slots.length; f++) {
            System.out.println("Floor " + f);

            for (int s = 0; s < slots[f].length; s++) {
                String status = slots[f][s] == null
                        ? "Available"
                        : slots[f][s].getRegNo();

                System.out.println("Slot " + s + ": " + status);
            }
        }
    }

    // Total collection
    public void displayCollection() {
        System.out.println("\nTotal Collection: Rs. " +
                totalCollection);
    }

    // Main method
    public static void main(String[] args) {

        // Three floors with different slot counts
        ParkingSystem parking =
                new ParkingSystem(new int[]{5, 4, 6});

        // Create passes
        ParkingPass studentPass =
                new ParkingPass(UserType.STUDENT, true);

        ParkingPass staffPass =
                new ParkingPass(UserType.STAFF, true);

        ParkingPass visitorPass =
                new ParkingPass(UserType.VISITOR, false);

        ParkingPass visitorOneDayPass =
                new ParkingPass(UserType.VISITOR, true);

        // Create vehicles
        Vehicle bike = new TwoWheeler("TN01AB1111");
        Vehicle car = new Car("TN02CD2222");
        Vehicle ev = new ElectricCar("TN03EF3333");
        Vehicle ebike = new EBike("TN04GH4444");
        Vehicle van = new DeliveryVan("TN05IJ5555");

        System.out.println("CAMPUS SMART PARKING SYSTEM");

        // Vehicle entries
        parking.vehicleEntry(bike, studentPass);
        parking.vehicleEntry(car, visitorPass);
        parking.vehicleEntry(ev, visitorPass);
        parking.vehicleEntry(ebike, visitorOneDayPass);
        parking.vehicleEntry(van, staffPass);

        // Display status
        parking.displayStatus();

        // Vehicle exits
        parking.vehicleExit("TN01AB1111", 0);
        parking.vehicleExit("TN02CD2222", 0);
        parking.vehicleExit("TN03EF3333", 10);
        parking.vehicleExit("TN04GH4444", 2);
        parking.vehicleExit("TN05IJ5555", 0);

        // Final collection
        parking.displayCollection();

        System.out.println("\n===== SYSTEM COMPLETED =====");
    }
}
