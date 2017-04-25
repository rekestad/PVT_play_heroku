package controllers;

import play.mvc.*;

import views.html.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class TestClass extends Controller {

	public Result helloworld(String input, String id) {
		return ok("Hello World! " + input + "\n" + id);
	}
}
