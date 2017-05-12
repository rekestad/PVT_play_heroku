package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.tools.SQLTools;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;

public class EventController extends Controller {
	private Database db;
	private SQLTools.ResultSetProcessor nullRp;

	@Inject
	public EventController(Database db) {
		this.db = db;
		nullRp = rs -> {
			
		};
	}
	
	// CREATE EVENT
	public Result createEvent() {
		JsonNode jNode = request().body().asJson();
		String sql = "INSERT INTO Events VALUES (NULL, ?, ?, ?, ?, ?, ?, 1)";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setInt(1, jNode.findPath("location_id").asInt());
			pstmt.setLong(2, jNode.findPath("user_id").asLong());
			pstmt.setString(3, jNode.findPath("date").textValue());
			pstmt.setString(4, jNode.findPath("start_time").textValue());
			pstmt.setString(5, jNode.findPath("end_time").textValue());
			pstmt.setString(6, jNode.findPath("description").textValue());
		};
		
		String sql2 = "INSERT INTO Event_attendees VALUES ((SELECT MAX(event_id) FROM Events WHERE Events.user_id = ?), ?)";

		SQLTools.StatementFiller sf2 = pstmt -> {
			pstmt.setLong(1, jNode.findPath("user_id").asLong());
			pstmt.setLong(2, jNode.findPath("user_id").asLong());
		};

		try {
			SQLTools.doPreparedStatement(db, sql, sf, nullRp);
			SQLTools.doPreparedStatement(db, sql2, sf2, nullRp);
		} catch (SQLException e) {
			return internalServerError("Error: " + e.toString());
		}

		return ok("Event created and user attended.");
	}

	// SELECT EVENT
	public Result selectEvent(int eventId) {
		final JsonNode[] result = { null };
		String sql = "SELECT DISTINCT Events.*, Locations.name, Location_types.type_name "
				+ "FROM Events, Locations, Location_types WHERE "
				+ "Events.event_id = ? AND Events.location_id = Locations.location_id AND "
				+ "Locations.location_type = Location_types.type_id";

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setInt(1, eventId);
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

	// SELECT EVENT BY LOCATION
	public Result selectEventsByLocation(int locationId) {
		final JsonNode[] result = { null };
		String sql = "SELECT DISTINCT Events.*, Locations.name, Location_types.type_name "
				+ "FROM Events, Locations, Location_types WHERE "
				+ "Locations.location_id = ? AND Events.location_id = Locations.location_id AND "
				+ "Locations.location_type = Location_types.type_id";

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setInt(1, locationId);
		};

		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, sql, sf, rp);
		} catch (SQLException e) {
			return internalServerError(e.toString());
		}

		return ok(result[0]);
	}

	// SELECT EVENT BY USER
	public Result selectEventsByUser(long userId) {
		final JsonNode[] result = { null };
		String sql = "SELECT DISTINCT Events.*, Locations.name, Location_types.type_name "
				+ "FROM Events, Locations, Location_types WHERE EXISTS "
				+ "(SELECT NULL FROM Event_attendees WHERE Event_attendees.event_id = Events.event_id AND "
				+ "Event_attendees.user_id = ?) AND " + "Events.location_id = Locations.location_id AND "
				+ "Locations.location_type = Location_types.type_id";

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setLong(1, userId);
		};

		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, sql, sf, rp);
		} catch (SQLException e) {
			return internalServerError(e.toString());
		}

		return ok(result[0]);
	}

	// SELECT EVENT ATTENDEES
	public Result selectEventAttendees(int eventId) {
		final JsonNode[] result = { null };
		String sql = "SELECT u.*, (SELECT GROUP_CONCAT(c.age) FROM User_children c WHERE c.parent_id = u.user_id) AS children "
				+ "FROM Users u WHERE EXISTS " + "(SELECT NULL FROM Event_attendees e WHERE e.event_id = ? AND "
				+ "e.user_id = u.user_id)";

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setInt(1, eventId);
		};

		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, sql, sf, rp);
		} catch (SQLException e) {
			return internalServerError(e.toString());
		}

		return ok(result[0]);
	}

	// SELECT EVENT CHAT
	public Result selectEventChat(int eventId) {
		final JsonNode[] result = { null };

		String sql = "SELECT CONCAT(Users.first_name, ' ', Users.last_name) AS name, message, date_format(date_time, '%Y-%m-%d kl %H:%i') date_time FROM Chats, Users WHERE Chats.event_id = ? AND Chats.user_id = Users.user_id ORDER BY Chats.date_time";

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setInt(1, eventId);
		};

		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, sql, sf, rp);
		} catch (SQLException e) {
			return internalServerError(e.toString());
		}

		return ok(result[0]);
	}
	
	// @With(SecuredAction.class)
	public Result insertEventChat() {

		JsonNode jNode = request().body().asJson();
		String sql = "INSERT INTO Chats (event_id, user_id, message) VALUES (?,?,?)";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setInt(1, jNode.findPath("event_id").asInt());
			pstmt.setLong(2, jNode.findPath("user_id").asLong());
			pstmt.setString(3, jNode.findPath("message").textValue());
		};

		try {
			SQLTools.doPreparedStatement(db, sql, sf, nullRp);
		} catch (SQLException e) {
			return internalServerError("Error: " + e.toString());
		}
		
		return ok("Chat message inserted");
	}
}

// package controllers;
//
// import com.fasterxml.jackson.databind.JsonNode;
// import controllers.tools.SQLTools;
//// import controllers.tools.SecuredAction;
// import play.db.Database;
// import play.mvc.Result;
//// import play.mvc.With;
//
// import java.sql.SQLException;
//
// import javax.inject.Inject;
//
// public class EventController extends AppController {
// private SQLTools.ResultSetProcessor returnEventRp;
//
// @Inject
// public EventController(Database db) {
// super(db);
//
// returnEventRp = rs -> {
// while (rs.next()) {
// int id = rs.getInt("event_id");
// int locationId = rs.getInt("location_id");
// String locationName = rs.getString("location_name");
// int userId = rs.getInt("user_id");
// String date = rs.getString("date");
// String startTime = rs.getString("start_time");
// String endTime = rs.getString("end_time");
// String desc = rs.getString("description");
// int status = rs.getInt("status");
//
// addReturnData("{\"event_id\":\"" + id + "\", \"location_id\":\"" + locationId
// + "\", \"location_name\":\"" + locationName + "\", \"user_id\":\"" + userId +
// "\", \"date\":\""
// + date + "\", \"start_time\":\"" + startTime + "\", \"end_time\":\"" +
// endTime
// + "\", \"description\":\"" + desc + "\", \"status\":\"" + status + "\"}");
// }
// };
// }
//
// public Result createEvent() {
// JsonNode jNode = request().body().asJson();
// String sql = "INSERT INTO Events VALUES (NULL, ?, ?, ?, ?, ?, ?, 1)";
// String sql2 = "INSERT INTO Event_attendees VALUES ((SELECT MAX(event_id) FROM
// Events), ?)";
//
// SQLTools.StatementFiller sf = pstmt -> {
// pstmt.setString(1, jNode.findPath("locationId").textValue());
// pstmt.setString(2, jNode.findPath("userId").textValue());
// pstmt.setString(3, jNode.findPath("date").textValue());
// pstmt.setString(4, jNode.findPath("startTime").textValue());
// pstmt.setString(5, jNode.findPath("startTime").textValue());
// pstmt.setString(6, jNode.findPath("description").textValue());
// };
//
// SQLTools.StatementFiller sf2 = pstmt -> {
// pstmt.setString(1, jNode.findPath("userId").textValue());
// };
//
// if (executeQuery(sql, sf, null)) {
// if (executeQuery(sql2, sf2, null))
// return created("Event created and user attended.");
// }
//
// return badRequest(getMessage());
// }
//
// public Result updateEvent() {
// JsonNode jNode = request().body().asJson();
// String sql = "UPDATE Events SET location_id = ?, date = ?, start_time = ?,
// end_time = ?, description = ? WHERE event_id = ? AND user_id = ?";
//
// SQLTools.StatementFiller sf = pstmt -> {
// pstmt.setString(1, jNode.findPath("locationId").textValue());
// pstmt.setString(2, jNode.findPath("date").textValue());
// pstmt.setString(3, jNode.findPath("startTime").textValue());
// pstmt.setString(4, jNode.findPath("endTime").textValue());
// pstmt.setString(5, jNode.findPath("description").textValue());
// pstmt.setString(6, jNode.findPath("eventId").textValue());
// pstmt.setString(7, jNode.findPath("userId").textValue());
// };
//
// if (executeQuery(sql, sf, null))
// return ok("Query executed.");
// else
// return badRequest(getMessage());
// }
//
// public Result cancelEvent() {
// JsonNode jNode = request().body().asJson();
// String sql = "UPDATE Events SET status = 0 WHERE event_id = ? AND user_id =
// ?";
//
// SQLTools.StatementFiller sf = pstmt -> {
// pstmt.setString(1, jNode.findPath("eventId").textValue());
// pstmt.setString(2, jNode.findPath("userId").textValue());
// };
//
// if (executeQuery(sql, sf, null))
// return ok("Query executed.");
// else
// return badRequest(getMessage());
// }
//
// //@With(SecuredAction.class)
// public Result selectEvent(int eventId) {
// final JsonNode[] result = {null};
// String sql = "SELECT DISTINCT `Events`.*, `Locations`.`name` AS
// `location_name` FROM `Events`, `Locations WHERE Events.event_id = ? AND
// Events.location_id = Locations.location_id";
//
// SQLTools.StatementFiller sf = stmt -> {
// stmt.setInt(1, eventId);
// };
//
// SQLTools.ResultSetProcessor rp = rs -> {
// result[0] = SQLTools.columnsAndRowsToJSON(rs);
// };
//
// try {
// SQLTools.doPreparedStatement(getDb(), sql, sf, rp);
// } catch (SQLException e) {
// return internalServerError("Error: " + e.toString());
// }
//
// return ok(result[0]);
// }
//
// public Result selectEventsByLocation(int locationId) {
// String sql = "SELECT DISTINCT Events.*, Locations.name AS location_name FROM
// Events, Locations WHERE Events.location_id = ? AND Events.location_id =
// Locations.location_id";
//
// SQLTools.StatementFiller sf = stmt -> {
// stmt.setInt(1, locationId);
// };
//
// if (executeQuery(sql, sf, returnEventRp))
// return ok(getReturnData());
// else
// return badRequest(getMessage());
// }
//
// public Result selectEventsByUser(int userId) {
//
// String sql = "SELECT DISTINCT Events.*, Locations.name AS location_name FROM
// Events, Locations WHERE Events.location_id = Locations.location_id AND EXISTS
// (SELECT * FROM Event_attendees WHERE user_id = ?)";
//
// SQLTools.StatementFiller sf = stmt -> {
// stmt.setInt(1, userId);
// };
//
// if (executeQuery(sql, sf, returnEventRp))
// return ok(getReturnData());
// else
// return badRequest(getMessage());
// }
//
// public Result selectEventsAtFavouriteLocations(int userId) {
// return ok("Undefined method.");
// }
//
// public Result addEventAttendee() {
// JsonNode jNode = request().body().asJson();
// String sql = "INSERT INTO Event_attendees VALUES (?, ?)";
//
// SQLTools.StatementFiller sf = pstmt -> {
// pstmt.setString(1, jNode.findPath("eventId").textValue());
// pstmt.setString(2, jNode.findPath("userId").textValue());
// };
//
// if (executeQuery(sql, sf, null))
// return created("Query executed.");
// else
// return badRequest(getMessage());
// }
//
// public Result deleteEventAttendee(int eventId, int userId) {
// return ok("Undefined method.");
// }
//
// public Result selectEventAttendees(int eventId) {
// final JsonNode[] result = {null};
// String sql = "SELECT DISTINCT Events.*, Locations.name AS location_name FROM
// Events, Locations WHERE Events.event_id = ? AND Events.location_id =
// Locations.location_id";
//
// SQLTools.StatementFiller sf = stmt -> {
// stmt.setInt(1, eventId);
// };
//
// SQLTools.ResultSetProcessor rp = rs -> {
// result[0] = SQLTools.columnsAndRowsToJSON(rs);
// };
//
// if (executeQuery(sql, sf, rp))
// return ok(result[0]);
// else
// return badRequest(getMessage());
// }
//
// public Result selectEventChat(int eventId) {
// final JsonNode[] result = {null};
//
// String sql = "SELECT CONCAT(Users.first_name, ' ', Users.last_name) AS name,
// message, date_format(date_time, '%Y-%m-%d kl %H:%i') date_time FROM Chats,
// Users WHERE Chats.event_id = ? AND Chats.user_id = Users.user_id ORDER BY
// Chats.date_time";
//
// SQLTools.StatementFiller sf = stmt -> {
// stmt.setInt(1, eventId);
// };
//
// SQLTools.ResultSetProcessor rp = rs -> {
// result[0] = SQLTools.columnsAndRowsToJSON(rs);
// };
//
// if (executeQuery(sql, sf, rp))
// return ok(result[0]);
// else
// return badRequest(getMessage());
// }
//
// //@With(SecuredAction.class)
// public Result insertEventChat(){
//
// JsonNode jNode = request().body().asJson();
// String sql = "INSERT INTO Chats (event_id, user_id, message) VALUES (?,?,?)";
//
// SQLTools.StatementFiller sf = pstmt -> {
// pstmt.setString(1, jNode.findPath("eventId").textValue());
// pstmt.setString(2, jNode.findPath("userId").textValue());
// pstmt.setString(3, jNode.findPath("message").textValue());
// };
//
// if (executeQuery(sql, sf, null))
// return created("Message added.");
// else
// return badRequest(getMessage());
// }
//
//
// }
