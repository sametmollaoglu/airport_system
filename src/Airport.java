import java.util.ArrayList;

public class Airport {
    public String name;
    public String where;
    public ArrayList<Flight> outGoing = new ArrayList<>();
    public ArrayList<Flight> inComing = new ArrayList<>();
    public boolean visited;

    public Airport(String name, String where) {

        this.name = name;
        this.where = where;
    }
}
