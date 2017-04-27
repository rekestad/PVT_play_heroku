package controllers;

import javax.inject.Inject;

import play.db.Database;
import play.mvc.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by felix on 2017-04-27.
 */
public class PlayDBTest extends Controller{
	private Database db;

	@Inject
	public PlayDBTest(Database db) {
		this.db = db;
	}

	public Result test() {
		final String[] result = {""};

		// my attempt to avoid all the try/catch statements (that still exist in doSQLStatement())
		StatementProcessor sp = stmt -> {
			ResultSet rs = stmt.executeQuery("SELECT * FROM Users");

			while(rs.next()){
				int id  = rs.getInt("User_ID");
				String fName = rs.getString("Name");
				String lName = rs.getString("Description");

				result[0] += "ID: " + id + ", name: " + fName + ", desc: " + lName + "\n";
			}

			rs.close();
		};
		doSQLStatement(sp);

		return ok(result[0]);
	}

	public void doSQLStatement(StatementProcessor sp) {
		Connection conn = db.getConnection();
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			sp.doQuery(stmt);

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

	private interface StatementProcessor {
		void doQuery(Statement stmt) throws SQLException;
	}
}
