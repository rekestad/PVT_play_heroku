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

	public Result createUser() {
		JsonNode jNode = request().body().asJson();

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, jNode.findPath("facebook_id").asLong());
			pstmt.setString(2, jNode.findPath("first_name").textValue());
			pstmt.setString(3, jNode.findPath("last_name").textValue());
		};

		SQLTools.ResultSetProcessor rp = rs -> {};

		try {
			SQLTools.doPreparedStatement(db, "INSERT INTO Users (facebook_id, first_name, last_name) VALUES (?,?,?)",
					sf, rp);
		} catch (SQLException e) {
			return internalServerError("couldn't make user: " + e);
		}

		return ok("made user!");

	}

	public Result getUser(long fbID) {
		final JsonNode[] result = {null};
		String fbIDStr = "" + fbID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, fbIDStr);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Users WHERE facebook_id=?", sf, rp);
		} catch (SQLException e) {
			return internalServerError("couldn't load user " + e);
		}

		return ok(result[0]);
	}

	public Result getAmountOfLikes(long fbID) {
		final JsonNode[] result = {null};
		String id2 = "" + fbID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, id2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT COUNT(*) AS likes FROM User_likes WHERE user_id2=?", sf,
					rp);
		} catch (SQLException e) {
			return internalServerError("couldn't load likes: " + e);
		}

		return ok(result[0]);
	}
}
