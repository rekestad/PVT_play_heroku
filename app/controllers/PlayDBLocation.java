package controllers;

import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.*;

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
                //int id  = rs.getInt("location_id");
                String name = rs.getString("name");
                int loc_type = rs.getInt("location_type");
                double x = rs.getDouble("position_x");
                double y = rs.getDouble("position_y");

                result[0] += "name: " + name + ", location_type: " + loc_type + ", position_x: " + x + ", location_y: " + y + "\n";
            }

            rs.close();
        };
        doSQLStatement(sp);

        return ok(result[0]);
    }

    /*public Result makeLocation(String name, int location_type, double position_x, double position_y) {
        Connection conn = db.getConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Locations (name, location_type, position_x, position_y) VALUES (?,?,?,?)");
            pstmt.setString(1, name);
            pstmt.setInt(2, location_type);
            pstmt.setDouble(3, position_x);
            pstmt.setDouble(4, position_y);
            pstmt.execute();
            conn.commit();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return ok("couldn't create user");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return ok("made user?");
    }*/

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
