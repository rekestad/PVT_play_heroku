package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.tools.SQLTools;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;

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

    // CHECK IF LOCATION IS USER FAVOURITE
    public Result checkIfFavourite(long userId, int locationId) {
        
        String sql = "SELECT IF(EXISTS(SELECT * FROM User_locations WHERE user_id = ? AND location_id = ?),'TRUE','FALSE') AS result";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setLong(1, userId);
            stmt.setInt(2, locationId);

        };

        SQLTools.ResultSetProcessor rp = rs -> {
            setReturnData(rs.getString("result"));
        };

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok(getReturnData());
    }
}



