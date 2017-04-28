package controllers;

import javax.inject.Inject;

import play.db.Database;
import play.mvc.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by felix on 2017-04-27.
 */
public class PlayDBTest extends Controller {
	private Database db;

	@Inject
	public PlayDBTest(Database db) {
		this.db = db;
	}

	public Result listUsrs() {
		final String[] result = {""};

//		// my attempt to avoid all the try/catch statements (that still exist in doSQLStatement())
//		StatementProcessor sp = stmt -> {
//			ResultSet rs = stmt.executeQuery("SELECT * FROM Users");
//
//			while(rs.next()){
//				int id  = rs.getInt("User_ID");
//				int age = rs.getInt("age");
//				String fName = rs.getString("Name");
//				String desc = rs.getString("Description");
//
//				result[0] += "ID: " + id + ", age: " + age + ", name: " + fName + ", desc: " + desc + "\n";
//			}
//
//			rs.close();
//		};
//		doSQLStatement(sp);

		return ok(result[0]);
	}

	public Result makeUsr(int age, String name, String desc) {
		Connection conn = db.getConnection();
		try {
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Users (Name, Age, Description) VALUES (?,?,?)");
			pstmt.setString(1, name);
			pstmt.setString(3, desc);
			pstmt.setInt(2, age);
			pstmt.execute();
			conn.commit();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return ok("couldn't create user");
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return ok("made user?");
	}
}
