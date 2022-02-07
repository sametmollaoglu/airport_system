import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main {

    static ArrayList<City> allCities = new ArrayList<>();    //all cities are stored here
    static ArrayList<Airport> allAirports = new ArrayList<>();    //all airports are stored here
    static ArrayList<Flight> allFlights = new ArrayList<>();    //all flights are stored here

    public static ArrayList<Airport> currentPath = new ArrayList<>();    //to store airport objects as vertices in DFS ()
    public static ArrayList<Flight> route = new ArrayList<>();    //to store flight objects as edges in DFS()
    public static ArrayList<ArrayList<Flight>> routes = new ArrayList<>();    //to store all possible routes between given cities

    static String output_string = "";    //All outputs are summed up in this variable to print to output.txt file

    public static void main(String[] args) {

        try {
            BufferedReader file = new BufferedReader(new FileReader(args[0]));
            String txtLine;
            while ((txtLine = file.readLine()) != null) {
                String[] airportListLine = txtLine.split("\t");

                ArrayList<Airport> tempCityAirports = new ArrayList<>(); //to store airport(s) of given city in each line
                for (int i = 1; i < airportListLine.length; i++) {
                    Airport airportTemp = new Airport(airportListLine[i], airportListLine[0]);
                    tempCityAirports.add(airportTemp);
                    allAirports.add(airportTemp);
                }
                City cityTemp = new City(airportListLine[0], tempCityAirports);
                allCities.add(cityTemp);
            }
            file.close();
        } catch (IOException a) {
            System.out.print(args[0] + " file cannot be read.\n");
        }

        try {
            BufferedReader file = new BufferedReader(new FileReader(args[1]));
            String txtLine;
            while ((txtLine = file.readLine()) != null) {
                String[] flightListLine = txtLine.split("\t");
                Flight flightTemp = new Flight(flightListLine[0], flightListLine[1], flightListLine[2], flightListLine[3], flightListLine[4]);
                allFlights.add(flightTemp);

                for (Airport airport : allAirports) {
                    if (flightListLine[1].split("->")[0].equals(airport.name)) {
                        airport.outGoing.add(flightTemp);  //added to airport outgoing edges arraylist as outgoing flight object
                    } else if (flightListLine[1].split("->")[1].equals(airport.name)) {
                        airport.inComing.add(flightTemp);  //added to airport incoming edges arraylist as incoming flight object
                    }
                }
            }
            file.close();
        } catch (IOException a) {
            System.out.print(args[1] + " file cannot be read.\n");
        }

        try {
            BufferedReader file = new BufferedReader(new FileReader(args[2]));
            String txtLine;
            while ((txtLine = file.readLine()) != null) {

                String[] commandListLine = txtLine.split("\t");
                Main.output_string = Main.output_string.concat("command : " + txtLine + "\n");
                if (commandListLine[0].equals("diameterOfGraph")) {

                    ArrayList<ArrayList<Flight>> shortestRoutes = new ArrayList<>(); //stores shortest routes between all pair of airports
                    for (Airport srcAirport : Main.allAirports) { //in these two for loops we find the shortest paths of all pair of airports
                        for (Airport dstAirport : Main.allAirports) {
                            if (srcAirport == dstAirport) {
                                continue;
                            }
                            DFS(srcAirport, dstAirport);  //DFS finds each possible route between airports
                            currentPath.clear();
                            route.clear();

                            if (Main.routes.size() > 0) {
                                ArrayList<Flight> shortestPath = new ArrayList<>(Main.routes.get(0));

                                for (ArrayList<Flight> path : Main.routes) {

                                    if (path.size() < shortestPath.size()) {
                                        shortestPath = path;
                                    } else if (path.size() == shortestPath.size()) {
                                        if (getPriceOfRoute(path) < getPriceOfRoute(shortestPath)) {
                                            shortestPath = path;
                                        }
                                    }
                                }

                                shortestRoutes.add(shortestPath); //just one path that the shortest among two given airports will be added to shortestRoutes
                            }

                            Main.routes.clear();
                        }

                    }

                    ArrayList<Flight> longestRoute = new ArrayList<>(shortestRoutes.get(0));

                    for (ArrayList<Flight> shortest : shortestRoutes) {
                        if (shortest.size() > longestRoute.size()) {
                            longestRoute = shortest;
                        }
                    }

                    Main.output_string = Main.output_string.concat("The diameter of graph : " + getPriceOfRoute(longestRoute) + "\n");


                } else if (commandListLine[0].equals("pageRankOfNodes")) {
                    Main.output_string = Main.output_string.concat("Not implemented\n");

                } else {
                    find(commandListLine);//this finds all the compatible routes for all the other commands


                    String a = commandListLine[2] + " " + "00:00"; //given start date in command is implemented here
                    SimpleDateFormat formatterFromDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    Date startDate = null;
                    try {
                        startDate = formatterFromDate.parse(a);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    ArrayList<ArrayList<Flight>> willRemoved = new ArrayList<>(); //stores if there any incompatible flight to start date
                    for (ArrayList<Flight> path : Main.routes) {
                        if (startDate != null) {
                            if (startDate.after(path.get(0).departureDateAndTime)) {
                                willRemoved.add(path);
                            }
                        }
                    }
                    Main.routes.removeAll(willRemoved);

                    //some initializations to use in several commands
                    ArrayList<Flight> shortestPath;
                    Date earliest;
                    ArrayList<Flight> cheapestPath;
                    int minPrice;
                    ArrayList<ArrayList<Flight>> filteredRoutes = new ArrayList<>();

                    switch (commandListLine[0]) {
                        case "listAll":

                            printListCommands(Main.routes);   //this prints all the compatible routes

                            break;

                        case "listProper":

                            filteredRoutes = findProperRoutes();   //proper routes are found and assigned
                            printListCommands(filteredRoutes);   //this prints all the compatible routes
                            break;

                        case "listCheapest":

                            cheapestPath = Main.routes.get(0);
                            minPrice = getPriceOfRoute(Main.routes.get(0));

                            for (ArrayList<Flight> path : Main.routes) {
                                if (getPriceOfRoute(path) < minPrice) {
                                    cheapestPath = path;
                                }
                            }
                            filteredRoutes.add(cheapestPath);

                            for (ArrayList<Flight> path : Main.routes) {
                                if (getPriceOfRoute(cheapestPath)== getPriceOfRoute(path) &&cheapestPath!=path) {
                                    filteredRoutes.add(path);
                                }
                            }

                            printListCommands(filteredRoutes);   //this prints all the compatible routes
                            break;

                        case "listQuickest":

                            shortestPath = Main.routes.get(0);
                            earliest = Main.routes.get(0).get(Main.routes.get(0).size() - 1).arrivalDateAndTime; //Date type variable stores earliest date that given in command

                            for (ArrayList<Flight> path : Main.routes) {
                                if (earliest.after(path.get(path.size() - 1).arrivalDateAndTime)) {
                                    shortestPath = path;
                                }
                            }
                            filteredRoutes.add(shortestPath);

                            for (ArrayList<Flight> path : Main.routes) {
                                if (shortestPath.get(shortestPath.size()-1).arrivalDateAndTime.equals(path.get(path.size()-1).arrivalDateAndTime)&&shortestPath!=path) {
                                    filteredRoutes.add(path);
                                }
                            }

                            printListCommands(filteredRoutes);   //this prints all the compatible routes
                            break;

                        case "listCheaper":

                            Main.routes = findProperRoutes();
                            for (ArrayList<Flight> path : Main.routes) {
                                if (getPriceOfRoute(path) < Integer.parseInt(commandListLine[3])) {
                                    filteredRoutes.add(path);
                                }
                            }
                            printListCommands(filteredRoutes);   //this prints all the compatible routes

                            break;

                        case "listQuicker":

                            Main.routes = findProperRoutes();
                            String quickerString = commandListLine[3].split(" ")[0] + " " + commandListLine[3].split(" ")[1]; //given earliest date in quicker command
                            SimpleDateFormat quickerFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            Date quickerDate = null;
                            try {
                                quickerDate = quickerFormatter.parse(quickerString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            for (ArrayList<Flight> path : Main.routes) {
                                if (quickerDate != null) {
                                    if (quickerDate.after(path.get(path.size() - 1).arrivalDateAndTime)) {
                                        filteredRoutes.add(path);
                                    }
                                }

                            }
                            printListCommands(filteredRoutes);   //this prints all the compatible routes

                            break;
                        case "listExcluding":

                            Main.routes = findProperRoutes();
                            for (ArrayList<Flight> path : Main.routes) {
                                boolean exist = false;
                                for (Flight flight : path) {
                                    if ((flight.ID.substring(0, 2).equals(commandListLine[3]))) {
                                        exist = true;  //if given unwanted airline company is exist in the path, exist variable turns true
                                        break;
                                    }
                                }
                                if (!exist) {  //if given unwanted airline company is not exist in the path, the path can be added to filteredRoutes
                                    filteredRoutes.add(path);
                                }
                            }
                            printListCommands(filteredRoutes);   //this prints all the compatible routes
                            break;
                        case "listOnlyFrom":
                            Main.routes = findProperRoutes();
                            for (ArrayList<Flight> path : Main.routes) {
                                boolean only = true;
                                for (Flight flight : path) {
                                    if (!(flight.ID.substring(0, 2).equals(commandListLine[3]))) {
                                        only = false;   //if there are other airline company instead of given company, only variable turns false
                                        break;
                                    }
                                }
                                if (only) {  //if there are not other airline company instead of given company, the path can be added to filteredRoutes
                                    filteredRoutes.add(path);
                                }
                            }
                            printListCommands(filteredRoutes);   //this prints all the compatible routes
                            break;
                    }
                }

                Main.output_string = Main.output_string.concat("\n\n");
            }
            file.close();
        } catch (IOException a) {
            System.out.print(args[2] + " file cannot be read.\n");
        }


        try {//this try catch block create output text file and write in it
            PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
            writer.print(output_string);
            writer.close();
        } catch (IOException e) {
            System.out.print("An error occurred.\n");
        }
    }


    public static void find(String[] commandListLine) {

        String departureCityName = commandListLine[1].split("->")[0];
        String arrivalCityName = commandListLine[1].split("->")[1];
        City departureCity = null;
        City arrivalCity = null;

        for (City city : allCities) {
            if (city.name.equals(departureCityName)) {
                departureCity = city;
            } else if (city.name.equals(arrivalCityName)) {
                arrivalCity = city;
            }
        }

        if (departureCity == arrivalCity) {
            return;
        }
        if (departureCity != null && arrivalCity != null) {
            for (Airport departureAirport : departureCity.airportList) {
                for (Airport arrivalAirport : arrivalCity.airportList) { //each airport pairs in two cities taken
                    for (Airport airport : allAirports) {
                        airport.visited = false;
                    }
                    if (departureAirport != null) {
                        DFS(departureAirport, arrivalAirport);
                    }
                }
            }
        }
    }

    public static void DFS(Airport departureAirport, Airport arrivalAirport) {

        departureAirport.visited = true;  //initial node(airport) marked as visited
        currentPath.add(departureAirport);  //initial node(airport) is added to currentPath

        if (departureAirport == arrivalAirport) {  //if wanted airport is found, we add the route to the Main.routes then go back one step for continue to search other possible route from previous node
            ArrayList<Flight> tempRoute = new ArrayList<>(route);

            if (!Main.routes.contains(tempRoute)) {
                Main.routes.add(tempRoute);
            }
            departureAirport.visited = false;
            currentPath.remove(currentPath.size() - 1);
            if (route.size() > 0) {
                route.remove(route.size() - 1);
            }
            return;
        }
        for (Flight outGoingFlight : departureAirport.outGoing) {

            if (route.size() > 0 && route.get(route.size() - 1).arrivalDateAndTime.after(outGoingFlight.departureDateAndTime)) { //checks if the departure date of the next flight is compatible to the arrival date of the current flight
                continue;
            }
            if (outGoingFlight.arrivalAirport.visited) { //checks if there is a loop
                continue;
            }
            boolean a = false;
            for (Airport visitedAirport : currentPath) {
                if (outGoingFlight.arrivalAirport.where.equals(visitedAirport.where)) {//checks if the next city is previously visited
                    a = true;
                    break;
                }
            }
            if (a) {
                continue;
            }
            Airport nextAirport = outGoingFlight.arrivalAirport;  //next airport is updated to pass as parameter of recursive function
            route.add(outGoingFlight);  //outgoing flight is added to route
            DFS(nextAirport, arrivalAirport);  //recursive
        }
        currentPath.remove(currentPath.size() - 1);  //last item of currentPath is removed to keep continuing search from previous node
        if (route.size() > 0) {
            route.remove(route.size() - 1);  //last item of route is removed
        }
        departureAirport.visited = false;
    }


    public static void printListCommands(ArrayList<ArrayList<Flight>> paths) {
        if (paths.size() == 0) {
            Main.output_string = Main.output_string.concat("No suitable flight plan is found\n");
        }
        for (ArrayList<Flight> path : paths) {

            int routePrice = getPriceOfRoute(path);

            for (int i = 0; i < path.size(); i++) {
                Main.output_string = Main.output_string.concat(path.get(i).ID + "\t" + path.get(i).departureAirport.name + "->" + path.get(i).arrivalAirport.name + (i == path.size() - 1 ? "" : "||"));
            }

            long duration = getDurationOfRoute(path);

            long diffMinutes = duration / (60 * 1000) % 60;
            long diffHours = duration / (60 * 60 * 1000);
            Main.output_string = Main.output_string.concat("\t" + (diffHours < 10 ? "0" : "") + diffHours + ":" + (diffMinutes < 10 ? "0" : "") + diffMinutes + "/" + routePrice);
            Main.output_string = Main.output_string.concat("\n");
        }

        currentPath.clear();
        route.clear();
        Main.routes.clear();
    }

    public static long getDurationOfRoute(ArrayList<Flight> path) {//this function returns calculate elapsed time of given path as long variable (millisecond)
        Date departureDate = path.get(0).departureDateAndTime;
        Date arrivalDate = path.get(path.size() - 1).arrivalDateAndTime;

        return arrivalDate.getTime() - departureDate.getTime();
    }

    public static int getPriceOfRoute(ArrayList<Flight> path) {//this function returns calculate price of given path
        int routePrice = 0;
        for (Flight flight : path) {
            routePrice += flight.price;
        }
        return routePrice;
    }

    public static ArrayList<ArrayList<Flight>> findProperRoutes() {
        ArrayList<Flight> shortestPath;
        Date earliestDate;
        ArrayList<Flight> cheapestPath;
        int minPrice;
        ArrayList<ArrayList<Flight>> properRoutes = new ArrayList<>();

        shortestPath = Main.routes.get(0);
        earliestDate = Main.routes.get(0).get(Main.routes.get(0).size() - 1).arrivalDateAndTime;

        cheapestPath = Main.routes.get(0);
        minPrice = getPriceOfRoute(Main.routes.get(0));

        for (ArrayList<Flight> path : Main.routes) {  //firstly the shortestPath and cheapestPath are found
            if (earliestDate.after(path.get(path.size() - 1).arrivalDateAndTime)) {
                earliestDate = path.get(path.size() - 1).arrivalDateAndTime;
                shortestPath = path;
            }
            if (getPriceOfRoute(path) < minPrice) {
                minPrice = getPriceOfRoute(path);
                cheapestPath = path;
            }
        }
        if (shortestPath == cheapestPath) { //if both are the same object
            properRoutes.add(shortestPath);
        } else {
            properRoutes.add(shortestPath);
            properRoutes.add(cheapestPath);
        }
        for (ArrayList<Flight> path : Main.routes) {  //then, if there is a path that cheaper from shortestPath and shorter from cheapestPath will be added to properRoutes
            if (cheapestPath.get(cheapestPath.size() - 1).arrivalDateAndTime.after(path.get(path.size() - 1).arrivalDateAndTime) && getPriceOfRoute(path) < getPriceOfRoute(shortestPath)) {
                if (!properRoutes.contains(path)) {
                    properRoutes.add(path);
                }
            }
        }
        return properRoutes;
    }

}
