import java.util.ArrayList;

public class City {
    public String name;
    public ArrayList<Airport> airportList;

    public City(String name, ArrayList<Airport> airportList) {

        this.name = name;
        this.airportList = airportList;
    }
}
