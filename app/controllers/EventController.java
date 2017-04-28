package controllers;

import java.sql.SQLException;

import javax.inject.Inject;

import controllers.tools.SQLTools;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

public class EventController extends Controller {
	private Database db;

	@Inject
	public EventController(Database db) {
		this.db = db;
	}

	public Result createEvent(int locationId, String date, String time, String desc) {
		String sql = "INSERT INTO Events VALUES (NULL, ?, ?, ?, ?)";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setInt(1, locationId);
			pstmt.setString(2, date);
			pstmt.setString(3, time);
			pstmt.setString(4, desc);
		};

		if (executeQuery(sql, sf))
			return created("Event created.");
		else
			return internalServerError("Error while creating event.");
	}

	public Result updateEvent(int eventId, int locationId, String date, String time, String desc) {
		return ok();
	}

	public Result deleteEvent(int eventId) {
		return ok();
	}

	public Result selectEvent(int eventId) {
		return ok();
	}

	public Result selectEventsByFavourite(int userId) {
		return ok();
	}

	public Result selectEventCloseToPosition(int x, int y) {
		return ok();
	}

	public Result addEventAttendee(int eventId, int userId) {
		return ok();
	}

	public Result selectEventAttendees(int eventId) {
		return ok();
	}

	private boolean executeQuery(String sql, SQLTools.StatementFiller sf) {
		
		SQLTools.ResultSetProcesser rp = rs -> {
		};

		try {
			SQLTools.doPreparedStatement(db, sql, sf, rp);
		} catch (SQLException e) {
			return false;
		}

		return true;
	}
}
