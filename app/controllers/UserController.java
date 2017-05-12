package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.tools.SQLTools;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;

/**
 * Created by felix on 2017-04-27.
 */
public class UserController extends Controller {
	private Database db;

	@Inject
	public UserController(Database db) {
		this.db = db;
	}

	// LIST ALL USERS
	public Result listUsrs() {
		final JsonNode[] result = {null};

		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Users", stmt -> {
			}, rp);
		} catch (SQLException e) {
			return internalServerError("couldn't list users" + e);
		}

		return ok(result[0]);
	}

	// CREATE USER
	public Result createUser() {
		JsonNode jNode = request().body().asJson();

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, jNode.findPath("user_id").asLong());
			pstmt.setString(2, jNode.findPath("first_name").textValue());
			pstmt.setString(3, jNode.findPath("last_name").textValue());
		};

		SQLTools.ResultSetProcessor rp = rs -> {};

		try {
			SQLTools.doPreparedStatement(db, "INSERT INTO Users (user_id, first_name, last_name) VALUES (?,?,?)",
					sf, rp);
		} catch (SQLException e) {
			return internalServerError("couldn't make user: " + e);
		}

		return ok("made user!");

	}

	// GET SELECTED USER
	public Result getUser(long fbID) {
		final JsonNode[] result = {null};
		String fbIDStr = "" + fbID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, fbIDStr);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Users WHERE user_id=?", sf, rp);
		} catch (SQLException e) {
			return internalServerError("couldn't load user " + e);
		}

		return ok(result[0]);
	}

	// GET USERS AMOUNT OF LIKES
	public Result getAmountOfLikes(long fbID) {
		final JsonNode[] result = {null};
		String id2 = "" + fbID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, id2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT COUNT(*) AS likes FROM User_likes WHERE liked_id=?", sf,
					rp);
		} catch (SQLException e) {
			return internalServerError("couldn't load likes: " + e);
		}

		return ok(result[0]);
	}

	// GET A USERS LOCATIONS (PROFILE VIEW)
	public Result getUserLocations(long userID){
		final JsonNode[] result = {null};
		String userID2 = "" + userID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, userID2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try{
			SQLTools.doPreparedStatement(db, "SELECT l.location_id, l.name FROM Locations AS l, User_locations AS ul WHERE l.location_id = ul.location_id AND ul.user_id = ?", sf, rp);
		} catch (SQLException e){
			return internalServerError("coudn't load users locations");
		}

		return ok(result[0]);
	}

	// GET A USERS LOCATIONS EVENTS
	public Result getUserLocationsEvents(long userID){
		final JsonNode[] result = {null};
		String userID2 = "" + userID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, userID2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try{
			SQLTools.doPreparedStatement(db, "SELECT l.location_id, l.name, e.* FROM Locations AS l, User_locations AS ul, Events AS e WHERE l.location_id = ul.location_id AND l.location_id =e.location_id AND ul.user_id = ?", sf, rp);
		} catch (SQLException e){
			return internalServerError("couldn't load users locations events");
		}

		return ok(result[0]);
	}
}
