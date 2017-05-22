package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.tools.SQLTools;
import controllers.tools.SecuredAction;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

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
			pstmt.setString(2, jNode.findPath("description").textValue());
		};

		try {
			SQLTools.doPreparedStatement(db, sql, sf, nullRp);
			SQLTools.doPreparedStatement(db, sql2, sf2, nullRp);
		} catch (SQLException e) {
			return internalServerError("Error: " + e.toString());
		}

		return ok("Event created and user attended.");
	}

	// UPDATE EVENT
	public Result updateEvent() {
		JsonNode jNode = request().body().asJson();
		String sql = "UPDATE Events SET location_id = ?, date = ?, start_time = ?, " +
				"end_time = ?, description = ? WHERE event_id = ? AND user_id = ?";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, jNode.findPath("location_id").textValue());
			pstmt.setString(2, jNode.findPath("date").textValue());
			pstmt.setString(3, jNode.findPath("start_time").textValue());
			pstmt.setString(4, jNode.findPath("end_time").textValue());
			pstmt.setString(5, jNode.findPath("description").textValue());
			pstmt.setString(6, jNode.findPath("event_id").textValue());
			pstmt.setString(7, jNode.findPath("user_id").textValue());
		};

		try {
			SQLTools.doPreparedStatement(db, sql, sf, nullRp);
		} catch (SQLException e) {
			return internalServerError("Error: " + e.toString());
		}

		return ok("Event updated.");

	}

	// CANCEL EVENT
	public Result cancelEvent() {
		JsonNode jNode = request().body().asJson();
		String sql = "UPDATE Events SET status = 0 WHERE event_id = ? AND user_id = ?";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, jNode.findPath("event_id").textValue());
			pstmt.setString(2, jNode.findPath("user_id").textValue());
		};

		try {
			SQLTools.doPreparedStatement(db, sql, sf, nullRp);
		} catch (SQLException e) {
			return internalServerError("Error: " + e.toString());
		}

		return ok("Event canceled.");
	}

	// DELETE EVENT
	public Result deleteEvent() {
		JsonNode jNode = request().body().asJson();
		String sql = "DELETE FROM Events WHERE event_id = ? AND user_id = ?";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setInt(1, jNode.findPath("event_id").asInt());
			pstmt.setLong(2, jNode.findPath("user_id").asLong());
		};

		try {
			SQLTools.doPreparedStatement(db, sql, sf, nullRp);
		} catch (SQLException e) {
			return internalServerError("Error: " + e.toString());
		}

		return ok("Event deleted.");
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
		String sql = "SELECT DISTINCT Events.*, Locations.name, Location_types.type_name " +
				"FROM Events, Locations, Location_types " +
				"WHERE Locations.location_id = ? " +
				"AND Events.location_id = Locations.location_id " +
				"AND Locations.location_type = Location_types.type_id " +
				"AND CONCAT(date, ' ', end_time) > NOW() " +
				"ORDER BY Events.date, Events.start_time";

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

	// CREATE EVENT ATTENDEE (Ska inte denna länkas med Users_children-tabellen?)
	public Result addEventAttendee(){
		JsonNode jNode = request().body().asJson();
		String sql = "INSERT INTO Event_attendees VALUES (?,?,?)";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setInt(1, jNode.findPath("event_id").asInt());
			pstmt.setLong(2, jNode.findPath("user_id").asLong());
			pstmt.setString(3, jNode.findPath("attending_children_ids").textValue());
		};

		try {
			SQLTools.doPreparedStatement(db, sql, sf, nullRp);
		} catch (SQLException e) {
			return internalServerError("Error: " + e.toString());
		}

		return ok("User attendee created.");
	}
	

	// DELETE ATTENDEE
	public Result deleteEventAttendee(){
		JsonNode jNode = request().body().asJson();
		String sql = "DELETE FROM Event_attendees WHERE event_id = ? AND user_id = ?";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setInt(1, jNode.findPath("event_id").asInt());
			pstmt.setLong(2, jNode.findPath("user_id").asLong());
		};

		try {
			SQLTools.doPreparedStatement(db, sql, sf, nullRp);
		} catch (SQLException e) {
			return internalServerError("Error: " + e.toString());
		}

		return ok("User attendee deleted.");
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

	// SELECT ALL EVENTS CREATED BY USER
	public Result selectEventsCreatedByUser(long userId){
		final JsonNode[] result = {null};
		String userID = "" + userId;

		String sql = "SELECT l.name_short, e.event_id, e.date, e.start_time, e.end_time, e.description, lt.type_name\n" +
				"FROM Users AS u, Events AS e, Locations AS l, Location_types AS lt \n" +
				"WHERE u.user_id = e.user_id\n" +
				"AND e.location_id = l.location_id\n" +
				"AND l.location_type = lt.type_id\n" +
				"AND u.user_id = ?";

		SQLTools.StatementFiller sf = stmt -> {stmt.setString(1, userID);};

		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try{
			SQLTools.doPreparedStatement(db, sql, sf, rp);
		} catch (SQLException e){
			return internalServerError(e.toString());
		}

		return ok(result[0]);
	}

	// SELECT EVENT CHAT
	public Result selectEventChat(int eventId) {
		final JsonNode[] result = { null };

		String sql = "SELECT CONCAT(Users.first_name, ' ', Users.last_name) AS name, message, " +
				"date_format(date_time, '%Y-%m-%d kl %H:%i') AS date_time FROM Chats, Users " +
				"WHERE Chats.event_id = ? AND Chats.user_id = Users.user_id ORDER BY Chats.date_time";

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

	// CREATE EVENT CHAT MESSAGE
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

	// ALEX REKESTAD TESTAR SÄKERHET
	@With(SecuredAction.class)
	public Result securityTest() {
		return ok("Method called.");
	}
}