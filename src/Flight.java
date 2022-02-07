import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Flight {
    public String ID;
    public Airport departureAirport;
    public Airport arrivalAirport;
    public Date departureDateAndTime;
    public Date arrivalDateAndTime;
    public String duration;
    public int price;

    public Flight(String ID, String between, String departureDateAndTime, String duration, String price) {

        this.ID = ID;
        for (Airport airport : Main.allAirports) {
            if (airport.name.equals(between.split("->")[0])) {
                this.departureAirport = airport;
            } else if (airport.name.equals(between.split("->")[1])) {
                this.arrivalAirport = airport;
            }
        }
        this.departureDateAndTime = setDepartureDateAndTime(departureDateAndTime);
        this.arrivalDateAndTime = setArrivalDateAndTime(departureDateAndTime, duration);
        this.duration = duration;
        this.price = Integer.parseInt(price);
    }

    private Date setDepartureDateAndTime(String departureDateAndTime) { //this function converts String type departureDate to Date type departureDate and returns it

        String sDate6 = departureDateAndTime.split(" ")[0] + " " + departureDateAndTime.split(" ")[1];
        SimpleDateFormat formatter6 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            return formatter6.parse(sDate6);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Date setArrivalDateAndTime(String departureDateAndTime, String duration) { //this function converts String type departureDate to Date type departureDate and returns it

        Date date6 = setDepartureDateAndTime(departureDateAndTime);
        Calendar c = Calendar.getInstance();
        if (date6 != null) {
            c.setTime(date6);
            c.add(Calendar.HOUR, Integer.parseInt(duration.split(":")[0]));
            c.add(Calendar.MINUTE, Integer.parseInt(duration.split(":")[1]));
        }
        return c.getTime();
    }
}
