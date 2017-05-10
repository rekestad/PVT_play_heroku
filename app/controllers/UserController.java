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
		final String[] result = { "[" };

		SQLTools.StatementFiller sf = stmt -> {
		};
		SQLTools.ResultSetProcessor rp = rs -> {
			while (rs.next()) {
				int id = rs.getInt("user_id");
				String fName = rs.getString("first_name");
				String lName = rs.getString("last_name");

				result[0] += "{ user_id: " + id + ", first_name: " + fName + ", last_name: " + lName + "}, \n";
			}
			result[0] += "]";
		};

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Users", sf, rp);
		} catch (SQLException e) {
			return ok("couldn't list users");
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

		SQLTools.ResultSetProcessor rp = rs -> {
		};

		try {
			SQLTools.doPreparedStatement(db, "INSERT INTO Users (facebook_id, first_name, last_name) VALUES (?,?,?)",
					sf, rp);
		} catch (SQLException e) {
			return ok("couldn't make user");
		}

		return ok("made user!");

	}

	public Result getUser(int id) {
		final String[] result = { "[" };
		String id2 = "" + id;

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setString(1, id2);
		};
		SQLTools.ResultSetProcessor rp = rs -> {
			while (rs.next()) {
				int userId = rs.getInt("user_id");
				String fname = rs.getString("first_name");
				String lname = rs.getString("last_name");

				result[0] += "{ \"user_id\":\"" + userId + "\" \"first_name\":\"" + fname + " \"last_name\":\"" + lname
						+ "  }, \n";
			}
			result[0] += "]";
		};

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Users WHERE user_id=?", sf, rp);
		} catch (SQLException e) {
			return ok("couldn't load user");
		}

		return ok(result[0]);
	}

	public Result getAmountOfLikes(int id) {
		final String[] result = { "[" };
		String id2 = "" + id;

		SQLTools.StatementFiller sf = stmt -> {
			stmt.setString(1, id2);
		};
		SQLTools.ResultSetProcessor rp = rs -> {
			while (rs.next() && !rs.isLast()) {
				int likes = rs.getInt("amount_of_likes");
				result[0] += "{ \"amount_of_likes\":\"" + likes + "\"  }, \n";
			}
			int likes = rs.getInt("amount_of_likes");
			result[0] += "{ \"amount_of_likes\":\"" + likes + "\"  } ]";
		};

		try {
			SQLTools.doPreparedStatement(db, "SELECT COUNT(*) AS amount_of_likes FROM User_likes WHERE user_id2=?", sf,
					rp);
		} catch (SQLException e) {
			return ok("couldn't load likes");
		}

		return ok(result[0]);
	}
}
