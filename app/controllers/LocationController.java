package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.tools.CoordinateConverter;
import controllers.tools.SQLTools;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;

public class LocationController extends Controller {
    private Database db;
    private String returnData;

    @Inject
    public LocationController(Database db) {
        this.db = db;
    }

    public Result listLocations() {
        final JsonNode[] result = {null};

        SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

        try {
            SQLTools.doPreparedStatement(db, "SELECT * FROM Locations", stmt -> {
            }, rp);
        } catch (SQLException e) {
            return internalServerError("couldn't list locations");
        }

        return ok(result[0]);
    }

    public String getReturnData() {
        String returnThis = returnData;
        returnData = "";

        return returnThis;
    }

    public void setReturnData(String returnData) {
        this.returnData = returnData;
    }

    public Result getLocation(int locationId) {
        final JsonNode[] result = {null};
        String sql = "SELECT * FROM Locations WHERE location_id = ?";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setInt(1, locationId);
        };

        SQLTools.ResultSetProcessor rp = rs -> {
            result[0] = SQLTools.columnsAndRowsToJSON(rs);
        };

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok(result[0]);
    }

    public Result searchLocations(String search) {
        final JsonNode[] result = {null};
        //search += "%";
        final String search2 = search;

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setString(1, search2);
        };

        SQLTools.ResultSetProcessor rp = rs -> {
            result[0] = SQLTools.columnsAndRowsToJSON(rs);
        };

        try {
            SQLTools.doPreparedStatement(db, "SELECT * FROM Locations WHERE name REGEXP ?", sf, rp);
        } catch (SQLException e) {
            return internalServerError("couldn't load search");
        }


        return ok(result[0]);
    }

    // LOCATIONS NEAR YOU
    public Result getLocationsNearYou(double lat, double lng) {
        final JsonNode[] result = {null};
        //search += "%";
        final double latitude = lat;
        final double longitude = lng;

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setDouble(1, latitude);
            stmt.setDouble(2, latitude);
            stmt.setDouble(3, longitude);
        };

        SQLTools.ResultSetProcessor rp = rs -> {
            result[0] = SQLTools.columnsAndRowsToJSON(rs);
        };

        try {
            SQLTools.doPreparedStatement(db, "SELECT location_id, name_short, location_type, round(6371 * 2 * ASIN(SQRT( POWER(SIN((? - abs(lat)) * pi()/180 / 2), 2) + COS(? * pi()/180 ) * COS(abs(lat) * pi()/180) * POWER(SIN((? - lng) * pi()/180 / 2), 2) )),1) AS distance FROM Locations HAVING distance < 3 ORDER BY distance LIMIT 20", sf, rp);
        } catch (SQLException e) {
            return internalServerError("couldn't load locations" + e);
        }


        return ok(result[0]);
    }

    // CHECK IF LOCATION IS USER FAVOURITE
    public Result checkIfFavourite(long userId, int locationId) {

        String sql = "SELECT IF(EXISTS(SELECT * FROM User_locations WHERE user_id = ? AND location_id = ?),'TRUE','FALSE') AS result";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setLong(1, userId);
            stmt.setInt(2, locationId);

        };

        SQLTools.ResultSetProcessor rp = rs -> {
            while(rs.next())
                setReturnData(rs.getString("result"));
        };

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok(getReturnData());
    }

    public Result convertCoordinate() {
        final JsonNode[] result = {null};
        String sql = "SELECT * FROM Locations LIMIT 20";

        CoordinateConverter cordi = new CoordinateConverter();


        ArrayList coord = new ArrayList<int[]>();
        final String[] results = null;

        SQLTools.StatementFiller sf = stmt -> { };

        SQLTools.ResultSetProcessor rp = rs -> {
            while (rs.next()) {
                int id = rs.getInt("location_id");
                int posX = rs.getInt("position_x");
                int posY = rs.getInt("position_y");

                //int[] data = {id,posX,posY};
                double[] coords = cordi.grid_to_geodetic(posX, posY);

                results[0] += "Location id" + id + ", Old X: " + posX + ", Old Y:" + posY + ", New X: " + coords[0] + ", New Y: " + coords[1] + "\n";
                //coord.add(data);
            }
        };

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok(results[0]);
    }
}



