package controllers;

import java.sql.SQLException;
import javax.inject.Inject;
import controllers.tools.SQLTools;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;


public class EventController extends Controller {
	private Database db;
	private String exceptionMessage;
	
	@Inject
    FormFactory fc;

	@Inject
	public EventController(Database db, FormFactory fc) {
		this.db = db;
		exceptionMessage = "";
	}

	public Result createEvent(String locationId, String date, String time, String description) {

		String sql = "INSERT INTO Events VALUES (NULL, ?, ?, ?)";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, locationId);
			pstmt.setString(2, date + " " + time);
			pstmt.setString(3, description);
		};

		if (executeQuery(sql, sf, null))
			return ok("Event created.");
		else
			return badRequest("Error: " + exceptionMessage);
	}
	
	// public Result updateEvent(int eventId, int locationId, String date,
	// String time, String description) {
	public Result updateEvent(String eventId, String locationId, String date, String time, String description) {
		String sql = "UPDATE Events SET location_id = ?, date_time = ?, description = ? WHERE event_id = ?";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, locationId);
			pstmt.setString(2, date + " " + time);
			pstmt.setString(3, description);
			pstmt.setString(4, eventId);
		};

		if (executeQuery(sql, sf, null))
			return ok("Event updated.");
		else
			return badRequest("Error: " + exceptionMessage);
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

	private boolean executeQuery(String sql, SQLTools.StatementFiller sf, SQLTools.ResultSetProcesser rp) {

		try {
			SQLTools.doPreparedStatement(db, sql, sf, rp);
		} catch (SQLException e) {
			exceptionMessage = e.toString();
			return false;
		}

		return true;
	}
	
	public Result postTest() {
		DynamicForm requestData = fc.form().bindFromRequest();
		
		
//        System.out.println("form: " + );
		
//	    if (form.data().size() == 0) {
//	        return badRequest("Expceting some data");
//	    } else {
//	        String response = "Client " + form.get("nome_cliente") + "has phone number " + form.get("telefone_cliente");
//	        return ok(response);
//	    }
		
	    return ok("Received: " + requestData.get("testpara"));
	}
}
