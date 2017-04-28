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

    public Result addLocation(String name, int loc_type, double x, double y) {
        locations.add(new Location(name, loc_type, x, y));
        return ok();
    }

    public Result getLocations() {
        JsonNode ret = Json.toJson(locations);
        return ok(ret);
    }

    private class Location {
        private int loc_type;
        private String name;
        private double x, y;

        Location(String name, int loc_type, double x, double y) {
            this.name = name;
            this.loc_type = loc_type;
            this.x = x;
            this.y = y;
        }

        public String getName() { return name; }

        public int getLoc_type() { return loc_type; }

        public double getX() { return x; }

        public double getY() { return y; }
    }

}

