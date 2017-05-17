package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.tools.SQLTools;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;

public class LocationController extends Controller {
	private Database db;
//	private ArrayList<JsonNode> returnArray;

	@Inject
	public LocationController(Database db) {
		this.db = db;
//		returnArray = new ArrayList<JsonNode>();
	}
	
//	private void addReturnData(JsonNode jn) {
//		returnArray.add(jn);
//	}
//
//	private String getReturnData() {
//		String returnData = "";
//
//		for(JsonNode jn : returnArray)
//			returnData += jn;
//		returnArray.clear();
//
//		//returnData.replaceAll("%][%", ",");
//
//		returnData = returnData.replace("][",",");
//
//		return returnData;
//	}

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

	public Result getLocation(int locationId) {
		final JsonNode[] result = {null};
		String sql = "SELECT l.location_id, lp.type_name, l.name, l.name_short, l.position_x, l.position_y, " +
				"e.event_id, e.date, e.start_time, e.end_time, e.description, " +
				"(SELECT Count(event_id) FROM Event_attendees WHERE event_id = e.event_id) AS attendees " +
				"FROM Locations l, Events e, Location_types lp " +
				"WHERE l.location_id = ? AND e.location_id = l.location_id AND l.location_type = lp.type_id";

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

//	public Result getLocationAndEvents(int locationId) {
//		final JsonNode[] result = {null};
//		String sqlLocation = "SELECT * FROM Locations WHERE location_id=?";
//		String sqlEvents = "SELECT e.*, (SELECT Count(event_id) FROM Event_attendees WHERE event_id = e.event_id) AS NumberOfAttendees FROM Events e WHERE e.location_id = ?";
//
//		SQLTools.StatementFiller sf = stmt -> {
//			stmt.setInt(1, locationId);
//		};
//
//		SQLTools.ResultSetProcessor rp = rs -> {
//			addReturnData(SQLTools.columnsAndRowsToJSON(rs));
//		};
//
//		try {
//			SQLTools.doPreparedStatement(db, sqlLocation, sf, rp);
//			SQLTools.doPreparedStatement(db, sqlEvents, sf, rp);
//		} catch (SQLException e) {
//			return internalServerError("Error: " + e.toString());
//		}
//
//		return ok(getReturnData());
//	}

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
		// hej
	}
}



