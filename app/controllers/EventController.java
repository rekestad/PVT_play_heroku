package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.tools.SQLTools;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;

public class EventController extends Controller {
	private Database db;
	private String exceptionMessage;
	private ArrayList<String> returnData;
	private SQLTools.ResultSetProcesser returnRp;

	@Inject
	FormFactory fc;

	@Inject
	public EventController(Database db, FormFactory fc) {
		this.db = db;
		exceptionMessage = "";
		returnData = new ArrayList<String>();

		returnRp = rs -> {
			while (rs.next()) {
				int id = rs.getInt("event_id");
				int locationId = rs.getInt("location_id");
				String locationName = rs.getString("location_name");
				int userId = rs.getInt("user_id");
				String date = rs.getString("date");
				String startTime = rs.getString("start_time");
				String endTime = rs.getString("end_time");
				String desc = rs.getString("description");

				addReturnData("{\"event_id\":\"" + id + "\", \"location_id\":\"" + locationId + "\", \"location_name\":\"" + locationName + "\", \"user_id\":\""
						+ userId + "\", \"date\":\"" + date + "\", \"start_time\":\"" + startTime
						+ "\", \"end_time\":\"" + endTime + "\", \"description\":\"" + desc + "\"}");
			}
		};
	}

	private void addReturnData(String data) {
		returnData.add(data);
	}

	private String getReturnData() {
		String str = "";

		if (!returnData.isEmpty()) {
			for (int i = 0; i < returnData.size(); i++) {
				str += returnData.get(i);
				if (i != (returnData.size() - 1))
					str += ", \n";
			}

			return str;
		} else {
			return "No records found.";
		}

	}

	public Result createEvent() {
		JsonNode jNode = request().body().asJson();
		String sql = "INSERT INTO Events VALUES (NULL, ?, ?, ?, ?, ?, ?, 1)";
		String sql2 = "INSERT INTO Event_attendees VALUES ((SELECT MAX(event_id) FROM Events), ?)";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, jNode.findPath("locationId").textValue());
			pstmt.setString(2, jNode.findPath("userId").textValue());
			pstmt.setString(3, jNode.findPath("date").textValue());
			pstmt.setString(4, jNode.findPath("startTime").textValue());
			pstmt.setString(5, jNode.findPath("startTime").textValue());
			pstmt.setString(6, jNode.findPath("description").textValue());
		};

		SQLTools.StatementFiller sf2 = pstmt -> {
			pstmt.setString(1, jNode.findPath("userId").textValue());
		};

		if (executeQuery(sql, sf, null)) {
			if (executeQuery(sql2, sf2, null))
				return created("Event created and user attended.");
		}

		return badRequest("Error: " + exceptionMessage);
	}

	public Result updateEvent() {
		JsonNode jNode = request().body().asJson();
		String sql = "UPDATE Events SET location_id = ?, date = ?, start_time = ?, end_time = ?, description = ? WHERE event_id = ? AND user_id = ?";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, jNode.findPath("locationId").textValue());
			pstmt.setString(2, jNode.findPath("date").textValue());
			pstmt.setString(3, jNode.findPath("startTime").textValue());
			pstmt.setString(4, jNode.findPath("endTime").textValue());
			pstmt.setString(5, jNode.findPath("description").textValue());
			pstmt.setString(6, jNode.findPath("eventId").textValue());
			pstmt.setString(7, jNode.findPath("userId").textValue());
		};

		if (executeQuery(sql, sf, null))
			return ok("Query executed.");
		else
			return badRequest("Error: " + exceptionMessage);
	}

	public Result cancelEvent() {
		JsonNode jNode = request().body().asJson();
		String sql = "UPDATE Events SET status = 0 WHERE event_id = ? AND user_id = ?";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, jNode.findPath("eventId").textValue());
			pstmt.setString(2, jNode.findPath("userId").textValue());
		};

		if (executeQuery(sql, sf, null))
			return ok("Query executed.");
		else
			return badRequest("Error: " + exceptionMessage);
	}

	public Result selectEvent(int eventId) {
		String sql = "SELECT DISTINCT Events.*, Locations.name AS location_name FROM Events, Locations WHERE Events.event_id = ? AND Events.location_id = Locations.location_id";

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setInt(1, eventId);
		};

		if (executeQuery(sql, sf, returnRp))
			return ok(getReturnData());
		else
			return badRequest("Error: " + exceptionMessage);
	}

	public Result selectUserAttendedEvents(int userId) {
		String sql = "SELECT DISTINCT Events.*, Locations.name AS location_name FROM Events, Locations WHERE Events.location_id = Locations.location_id AND EXISTS (SELECT * FROM Event_attendees WHERE user_id = ?)";

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setInt(1, userId);
		};

		if (executeQuery(sql, sf, returnRp))
			return ok(getReturnData());
		else
			return badRequest("Error: " + exceptionMessage);
	}

	public Result selectEventsAtFavouriteLocations(int userId) {
		return ok("Undefined method.");
	}

	public Result addEventAttendee() {
		JsonNode jNode = request().body().asJson();
		String sql = "INSERT INTO Event_attendees VALUES (?, ?)";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, jNode.findPath("eventId").textValue());
			pstmt.setString(2, jNode.findPath("userId").textValue());
		};

		if (executeQuery(sql, sf, null))
			return created("Query executed.");
		else
			return badRequest("Error: " + exceptionMessage);
	}
	
	public Result deleteEventAttendee(int eventId, int userId) {
		return ok("Undefined method.");
	}
	
	public Result selectEventAttendees(int eventId) {
		return ok("Undefined method.");
	}

	private boolean executeQuery(String sql, SQLTools.StatementFiller sf, SQLTools.ResultSetProcesser rp) {
		if (rp == null) {
			rp = rs -> {
			};
		}

		if (sf == null) {
			sf = pstmt -> {
			};
		}

		try {
			SQLTools.doPreparedStatement(db, sql, sf, rp);
		} catch (SQLException e) {
			exceptionMessage = e.toString();
			return false;
		}

		return true;
	}

	public Result getChat(int eventId){
		final String[] result = {"["};
		String id2 = ""+eventId;

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setString(1, id2);
		};
		SQLTools.ResultSetProcesser rp = rs -> {
			while (rs.next()){
				//int event_id  = rs.getInt("event_id");
				int userId = rs.getInt("user_id");
				String message = rs.getString("message");

				result[0] += "{ \"user_id\":\""+userId+"\" \"message\":\""+message+"\", \n";
			}
			result[0] += "]";
		};

		try{
			SQLTools.doPreparedStatement(db, "SELECT * FROM `Chat` WHERE event_id = ?", sf, rp);
		}catch(SQLException e){
			return ok("couldn't load chat");
		}

		return ok(result[0]);
	}

//	public Result postTest() {
//		if (authenticateRequest(request().getHeader(AUTHORIZATION))) {
//			return ok("User authenticated OK!");
//		} else {
//			return ok("User NOT OK!");
//		}
//	}
}
