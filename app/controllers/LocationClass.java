package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;


/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class LocationClass extends Controller {
    ArrayList<Location> locations = new ArrayList<>();

    public Result helloworld(String input, String id) {
        return ok("Hello World! " + input + "\n" + id);
    }

    public Result addLocation(int id, String name, int loc_type, double x, double y) {
        locations.add(new Location(id, name, loc_type, x, y));
        return ok();
    }

    public Result getUsers() {
        JsonNode ret = Json.toJson(locations);
        return ok(ret);
    }

    private class Location {
        private int id, loc_type;
        private String name;
        private double x, y;

        Location(int id, String name, int loc_type, double x, double y) {
            this.id = id;
            this.name = name;
            this.loc_type = loc_type;
            this.x = x;
            this.y = y;
        }

        public int getId() { return id; }

        public String getName() { return name; }

        public int getLoc_type() { return loc_type; }

        public double getX() { return x; }

        public double getY() { return y; }
    }

}

