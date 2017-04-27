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

	public Result listUsrs() {
		final String[] result = {""};

		// my attempt to avoid all the try/catch statements (that still exist in doSQLStatement())
		StatementProcessor sp = stmt -> {
			ResultSet rs = stmt.executeQuery("SELECT * FROM Users");

			while(rs.next()){
				int id  = rs.getInt("User_ID");
				int age = rs.getInt("age");
				String fName = rs.getString("Name");
				String desc = rs.getString("Description");

				result[0] += "ID: " + id + ", age: " + age + ", name: " + fName + ", desc: " + desc + "\n";
			}

			rs.close();
		};
		doSQLStatement(sp);

		return ok(result[0]);
	}

	public Result makeUsr(int id, int age, String name, String desc) {
		StatementProcessor sp = stmt -> {
			stmt.executeUpdate("INSERT INTO Users VALUES ("+id+",\""+name+"\","+age+",\""+desc+"\")");
		};
		doSQLStatement(sp);
		return listUsrs();
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
