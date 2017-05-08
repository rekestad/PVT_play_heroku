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
		final String[] result = {""};

		SQLTools.StatementFiller sf = stmt -> {
		};
		SQLTools.ResultSetProcesser rp = rs -> {
			result[0] = getAllColumnsAndRow(rs);
		};

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Locations", sf, rp);
		} catch (SQLException e) {
			return ok("couldn't list locations");
		}

		return ok(result[0]);
	}

	public Result getLocation(int id) {
		final String[] result = {""};
		String id2 = "" + id;

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setString(1, id2);
		};
		SQLTools.ResultSetProcesser rp = rs -> {
			result[0] = getAllColumnsAndRow(rs);
		};

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Locations WHERE location_id=?", sf, rp);
		} catch (SQLException e) {
			return ok("couldn't load location");
		}

		return ok(result[0]);
	}

	public Result searchLocations(String search) {
		final String[] result = {""};
		search += "%";
		final String search2 = search;

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setString(1, search2);
		};
		SQLTools.ResultSetProcesser rp = rs -> {
			result[0] = getAllColumnsAndRow(rs);
		};

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Locations WHERE name LIKE ?", sf, rp);
		} catch (SQLException e) {
			return ok("couldn't load search");
		}


		return ok(result[0]);
	}

	private String getAllColumnsAndRow(ResultSet rs) throws SQLException {
		String result = "[";
		ResultSetMetaData metaData = rs.getMetaData();

		String[] columns = new String[metaData.getColumnCount()];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = metaData.getColumnName(i + 1);
		}

		boolean hasNext = rs.next();
		if (hasNext) {
			while (hasNext && !rs.isLast()){
				result += "{ ";

				for (int i = 0; i < columns.length; i++) {
					result += " \"" + columns[i] + "\": \"" + rs.getString(columns[i]) + "\", ";
				}

				result += "  }, \n";
				hasNext = rs.next();
			}

			result += "{ ";
			for (int i = 0; i < columns.length; i++) {
				result += " \"" + columns[i] + "\": \"" + rs.getString(columns[i]) + "\", ";
			}
			result += " }]";
		}
		return result;
	}

}



