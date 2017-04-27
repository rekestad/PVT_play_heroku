package controllers;

import play.mvc.*;

import java.sql.*;

/**
 * Created by felix on 2017-04-27.
 * Majority of code taken from https://www.tutorialspoint.com/jdbc/jdbc-sample-code.htm
 */
public class TestDBController extends Controller{
	// JDBC driver name and database URL
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://mysql.dsv.su.se/fetr0498";

	//  Database credentials
	private static final String USER = "fetr0498";
	private static final String PASS = "Teer6vahHaek";

	public Result openDatabase() {
		String result = "";
		Connection conn = null;
		Statement stmt = null;
		try{
			//STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql = "SELECT * FROM Users";
			ResultSet rs = stmt.executeQuery(sql);

			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				int id  = rs.getInt("User_ID");
				int age = rs.getInt("Age");
				String name = rs.getString("Name");
				String desc = rs.getString("Description");

				//Display values
				result += "ID: " + id + ", Age: " + age + ", name: " + name + ", desc: " + desc + "\n";
				System.out.print("ID: " + id);
				System.out.print(", Age: " + age);
				System.out.print(", name: " + name);
				System.out.println(", desc: " + desc);
			}
			//STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(stmt!=null)
					stmt.close();
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}//end try
		System.out.println("Goodbye!");

		return ok(result);
	}
}
