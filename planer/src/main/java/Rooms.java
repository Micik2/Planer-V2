import java.util.ArrayDeque;

/**
 * Created by Maciej on 2017-04-01.
 */
public class Rooms {
    private int idRoom;
    private int numberOfSeats;
    private ArrayDeque<Rooms> allRooms = new ArrayDeque<Rooms>();

    public Rooms(int numberOfSeats, ArrayDeque<Rooms> allRooms) {
        this.numberOfSeats = numberOfSeats;
        this.allRooms = allRooms;
        if (allRooms.isEmpty())
            this.idRoom = 0;
        else
            this.idRoom = allRooms.getLast().idRoom + 1;
        allRooms.add(new Rooms(idRoom, numberOfSeats));
    }

    public Rooms(int idR, int nOS) {
        this.idRoom = idR;
        this.numberOfSeats = nOS;
    }

    public ArrayDeque<Rooms> getAllRooms() {
        return allRooms;
    }

    public int getId() {
        return idRoom;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }
}
