package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.tools.SQLTools;
import controllers.tools.SecuredAction;
import play.db.Database;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;

public class EventController extends AppController {
	private SQLTools.ResultSetProcesser returnEventRp;

	@Inject
	public EventController(Database db) {
		super(db);

		returnEventRp = rs -> {
			while (rs.next()) {
				int id = rs.getInt("event_id");
				int locationId = rs.getInt("location_id");
				String locationName = rs.getString("location_name");
				int userId = rs.getInt("user_id");
				String date = rs.getString("date");
				String startTime = rs.getString("start_time");
				String endTime = rs.getString("end_time");
				String desc = rs.getString("description");
				int status = rs.getInt("status");

				addReturnData("{\"event_id\":\"" + id + "\", \"location_id\":\"" + locationId
						+ "\", \"location_name\":\"" + locationName + "\", \"user_id\":\"" + userId + "\", \"date\":\""
						+ date + "\", \"start_time\":\"" + startTime + "\", \"end_time\":\"" + endTime
						+ "\", \"description\":\"" + desc + "\", \"status\":\"" + status + "\"}");
			}
		};
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

		return badRequest(getMessage());
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
			return badRequest(getMessage());
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
			return badRequest(getMessage());
	}
	
	//@With(SecuredAction.class)
	public Result selectEvent(int eventId) {
		String sql = "SELECT DISTINCT Events.*, Locations.name AS location_name FROM Events, Locations WHERE Events.event_id = ? AND Events.location_id = Locations.location_id";

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setInt(1, eventId);
		};

		if (executeQuery(sql, sf, returnEventRp))
			return ok(getReturnData());
		else
			return badRequest(getMessage());
	}

	public Result selectEventsByLocation(int locationId) {
		String sql = "SELECT DISTINCT Events.*, Locations.name AS location_name FROM Events, Locations WHERE Events.location_id = ? AND Events.location_id = Locations.location_id";

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setInt(1, locationId);
		};

		if (executeQuery(sql, sf, returnEventRp))
			return ok(getReturnData());
		else
			return badRequest(getMessage());
	}

	public Result selectEventsByUser(int userId) {
		String sql = "SELECT DISTINCT Events.*, Locations.name AS location_name FROM Events, Locations WHERE Events.location_id = Locations.location_id AND EXISTS (SELECT * FROM Event_attendees WHERE user_id = ?)";

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setInt(1, userId);
		};

		if (executeQuery(sql, sf, returnEventRp))
			return ok(getReturnData());
		else
			return badRequest(getMessage());
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
			return badRequest(getMessage());
	}

	public Result deleteEventAttendee(int eventId, int userId) {
		return ok("Undefined method.");
	}

	public Result selectEventAttendees(int eventId) {
		return ok("Undefined method.");
	}

	public Result selectEventChat(int eventId) {
		String sql = "SELECT CONCAT(Users.first_name, ' ', Users.last_name) AS name, message, date_format(date_time, '%Y-%m-%d kl %H:%i') date_time FROM Chats, Users WHERE Chats.event_id = ? AND Chats.user_id = Users.user_id ORDER BY Chats.date_time";
		SQLTools.StatementFiller sf = stmt -> {
			stmt.setInt(1, eventId);
		};
		SQLTools.ResultSetProcesser rp = rs -> {
			while (rs.next()) {
				String name = rs.getString("name");
				String message = rs.getString("message");
				String dateTime = rs.getString("date_time");

				addReturnData("{ \"name\":\"" + name + "\" \"message\":\"" + message + "\" \"date_time\":\""
						+ dateTime + "\"}");
			}
		};

		if (executeQuery(sql, sf, rp))
			return ok(getReturnData());
		else
			return badRequest(getMessage());
	}
	
	//@With(SecuredAction.class)
	public Result insertEventChat(){

		JsonNode jNode = request().body().asJson();
		String sql = "INSERT INTO Chats (event_id, user_id, message) VALUES (?,?,?)";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, jNode.findPath("eventId").textValue());
			pstmt.setString(2, jNode.findPath("userId").textValue());
			pstmt.setString(3, jNode.findPath("message").textValue());
		};

		if (executeQuery(sql, sf, null))
			return created("Message added.");
		else
			return badRequest(getMessage());
	}
}
