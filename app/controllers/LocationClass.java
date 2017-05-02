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

    public Result addLocation(String sthlm_id, String name, String short_name, int loc_type, double x, double y) {
        locations.add(new Location(sthlm_id, name, short_name, loc_type, x, y));
        return ok();
    }

    public Result getLocations() {
        JsonNode ret = Json.toJson(locations);
        return ok(ret);
    }

    private class Location {
        private int loc_type;
        private String sthlm_id, name, name_short;
        private double x, y;

        Location(String sthlm_id, String name, String name_short, int loc_type, double x, double y) {
            this.sthlm_id = sthlm_id;
            this.name = name;
            this.name_short = name_short;
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

