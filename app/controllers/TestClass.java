package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.*;
import play.libs.Json;

import java.util.ArrayList;


/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class TestClass extends Controller {
	ArrayList<User> users = new ArrayList<>();

	public Result helloworld(String input, String id) {
		return ok("Hello World! " + input + "\n" + id);
	}

	public Result addUser(String fName, String lName, int age) {
		users.add(new User(fName, lName, age));
		return ok();
	}

	public Result getUsers() {
		JsonNode ret = Json.toJson(users);
		return ok(ret);
	}

	private class User {
		private String fName, lName;
		private int age;

		User(String fName, String lName, int age) {
			this.fName = fName;
			this.lName = lName;
			this.age = age;
		}

		public String getFullname() {
			return fName+" "+lName;
		}

		public String getfName() {
			return fName;
		}

		public String getlName() {
			return lName;
		}

		public int getAge() {
			return age;
		}
	}

}
