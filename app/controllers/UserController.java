package controllers;

import javax.inject.Inject;

import controllers.tools.SQLTools;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

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
		final String[] result = {"["};

		SQLTools.StatementFiller sf = stmt -> {};
		SQLTools.ResultSetProcesser rp = rs -> {
			while(rs.next()){
				int id  = rs.getInt("user_id");
				String fName = rs.getString("first_name");
				String lName = rs.getString("last_name");

				result[0] += "{ user_id: " + id + ", first_name: " + fName + ", last_name: "+ lName + "}, \n";
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

	public Result addUsr(long id, String fname, String lname) {
		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, id);
			pstmt.setString(3, fname);
			pstmt.setString(2, lname);
		};

		SQLTools.ResultSetProcesser rp = rs -> {
		};

		try {
			SQLTools.doPreparedStatement(db, "INSERT INTO Users (facebook_id, first_name, last_name) VALUES (?,?,?)", sf, rp);
		} catch (SQLException e) {
			return ok("couldn't make user");
		}

		return ok("made user?");
	}
}

