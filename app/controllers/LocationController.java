package controllers;

import com.fasterxml.jackson.databind.JsonNode;
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
		final JsonNode[] result = {null};

		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Locations", stmt -> {}, rp);
		} catch (SQLException e) {
			return internalServerError("couldn't list locations");
		}

		return ok(result[0]);
	}

	public Result getLocation(int id) {
		final JsonNode[] result = {null};
		String id2 = "" + id;

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setString(1, id2);
		};
		SQLTools.ResultSetProcessor rp = rs -> {
			result[0] = SQLTools.columnsAndRowsToJSON(rs);
		};

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Locations WHERE location_id=?", sf, rp);
		} catch (SQLException e) {
			return internalServerError("couldn't load location");
		}

		return ok(result[0]);
	}

	public Result searchLocations(String search) {
		final JsonNode[] result = {null};
		search += "%";
		final String search2 = search;

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setString(1, search2);
		};
		
		SQLTools.ResultSetProcessor rp = rs -> {
			result[0] = SQLTools.columnsAndRowsToJSON(rs);
		};

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Locations WHERE name LIKE ?", sf, rp);
		} catch (SQLException e) {
			return internalServerError("couldn't load search");
		}


		return ok(result[0]);
	}

}



