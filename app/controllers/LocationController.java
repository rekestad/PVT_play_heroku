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

                result[0] += "{ \"location_id\":\""+id+"\" \"name_short\":\""+name_short+"\" \"location_type\":\""+loc_type+"\" \"position_x\":\""+x+"\" \"location_y\":\""+y+"\", \n";
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

    public Result getLocation(int id){
        final String[] result = {"["};
        String id2 = ""+id;

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setString(1, id2);
        };
        SQLTools.ResultSetProcesser rp = rs -> {
            while (rs.next()){
                int locationId  = rs.getInt("location_id");
                String name_short = rs.getString("name_short");
                int loc_type = rs.getInt("location_type");
                double x = rs.getDouble("position_x");
                double y = rs.getDouble("position_y");

                result[0] += "{ \"location_id\":\""+locationId+"\" \"name_short\":\""+name_short+"\" \"location_type\":\""+loc_type+"\" \"position_x\":\""+x+"\" \"location_y\":\""+y+"\", \n";
            }
            result[0] += "]";
        };

        try{
            SQLTools.doPreparedStatement(db, "SELECT * FROM Locations WHERE location_id=?", sf, rp);
        }catch(SQLException e){
            return ok("couldn't load location");
        }

        return ok(result[0]);
    }

    public Result searchLocations(String search){
        final String[] result = {"["};
        search += "%";
        final String search2 = search;

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setString(1, search2);
        };
        SQLTools.ResultSetProcesser rp = rs -> {
            while (rs.next() && !rs.isLast()){
                String name = rs.getString("name");
                result[0] += "{ \"name\":\""+name+"\"  }, \n";
            }
            String name = rs.getString("name");
            result[0] += "{ \"name\":\""+name+"\"  } ]";
        };

        try{
            SQLTools.doPreparedStatement(db, "SELECT name FROM Locations WHERE name LIKE ?", sf, rp);
        }catch(SQLException e){
            return ok("couldn't load search");
        }

        return ok(result[0]);
    }

}



