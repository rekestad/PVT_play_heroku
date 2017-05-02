package controllers;

import controllers.tools.SQLTools;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.*;

public class LocationController extends Controller {
    private Database db;

    @Inject
    public LocationController(Database db) {
        this.db = db;
    }

    public Result listLocations() {
        final String[] result = {"["};

        SQLTools.StatementFiller sf = stmt -> {};
        SQLTools.ResultSetProcesser rp = rs -> {
            while(rs.next()){
                int id  = rs.getInt("location_id");
                String name_short = rs.getString("name_short");
                int loc_type = rs.getInt("location_type");
                double x = rs.getDouble("position_x");
                double y = rs.getDouble("position_y");

                result[0] += "{ location_id: $id, name_short: ${name_short}, location_type: $loc_type, position_x: $x, location_y: $y}, \n";
            }
            result[0] += "]";
        };

        try{
            SQLTools.doPreparedStatement(db, "SELECT * FROM Locations", sf, rp);
        } catch (SQLException e) {
            return ok("couldn't list locations");
        }

        return ok(result[0]);
    }

    public Result addLocation(String sthlm_id, String name, String name_short, int location_type, double x, double y){
        SQLTools.StatementFiller sf = pstmt -> {
            pstmt.setString(2, sthlm_id);
            pstmt.setString(3, name);
            pstmt.setString(4, name_short);
            pstmt.setInt(5, location_type);
            pstmt.setDouble(6, x);
            pstmt.setDouble(7, y);
        };

        SQLTools.ResultSetProcesser rp = rs -> {
        };

        try{
            SQLTools.doPreparedStatement(db, "INSERT INTO Locations (sthlm_id, name, name_short, location_type, position_x, position_y) VALUES (?,?,?,?,?,?)", sf, rp);
        } catch (SQLException e){
            return ok("couldn't make location");
        }

        return ok("made location");
    }

}



