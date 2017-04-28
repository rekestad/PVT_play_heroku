package controllers.tools;

import play.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by felix on 2017-04-28.
 */
public class SQLTools {

	/**
	 * @param db database connection pool
	 * @param query prepared query statment to be filled in by StatementFiller sf
	 * @param sf fills in the PreparedStatement
	 * @param rp processes the ResultSet
	 * @throws SQLException if the query or StatementFiller was faulty
	 */
	public static void doPreparedStatement(Database db, String query, StatementFiller sf, ResultSetProcesser rp) throws SQLException {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		try {
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(query);
			sf.fillStatement(pstmt);
			pstmt.execute();
			rp.processResultSet(pstmt.getResultSet());
			conn.commit();
		} finally {
			// closing connection
			try {
				if (pstmt != null)
					pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public interface StatementFiller {
		void fillStatement(PreparedStatement pstmt) throws SQLException;
	}

	public interface ResultSetProcesser {
		void processResultSet(ResultSet rs) throws SQLException;
	}
}
