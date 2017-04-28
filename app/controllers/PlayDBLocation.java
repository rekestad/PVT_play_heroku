package controllers;

import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Cecilia on 2017-04-28.
 */

public class PlayDBLocation extends Controller {
    private Database db;

    @Inject
    public PlayDBLocation(Database db) {
        this.db = db;
    }

    public Result listLocations() {
        final String[] result = {""};

        // my attempt to avoid all the try/catch statements (that still exist in doSQLStatement())
        StatementProcessor sp = stmt -> {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Locations");

            while(rs.next()){
                int id  = rs.getInt("location_id");
                String name = rs.getString("name");
                int loc_type = rs.getInt("location_type");
                double x = rs.getDouble("position_x");
                double y = rs.getDouble("position_y");

                result[0] += "location_id: " + id + ", name: " + name + ", location_type: " + loc_type + ", position_x: " + x + ", location_y: " + y + "\n";
            }

            rs.close();
        };
        doSQLStatement(sp);

        return ok(result[0]);
    }

    public Result makeLocation(int id, String name, int loc_type, double x, double y) {
        StatementProcessor sp = stmt -> {
            stmt.executeUpdate("INSERT INTO Locations VALUES ("+id+",\""+name+"\","+loc_type+","+x+", "+y+")");
        };
        doSQLStatement(sp);
        return listLocations();
    }

    public void doSQLStatement(StatementProcessor sp) {
        Connection conn = db.getConnection();
        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            sp.doQuery(stmt);

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    private interface StatementProcessor {
        void doQuery(Statement stmt) throws SQLException;
    }

}
