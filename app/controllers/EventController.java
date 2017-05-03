package controllers;

import java.sql.SQLException;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.tools.SQLTools;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

public class EventController extends Controller {
	private Database db;
	private String exceptionMessage;

	@Inject
	FormFactory fc;

	@Inject
	public EventController(Database db, FormFactory fc) {
		this.db = db;
		exceptionMessage = "";
	}

	public Result createEvent() {
		JsonNode jNode = request().body().asJson();
		//String name = js.findPath("name").textValue();
		
		//DynamicForm requestData = fc.form().bindFromRequest();
		String sql = "INSERT INTO Events VALUES (NULL, ?, ?, ?)";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, jNode.findPath("locationId").textValue());
			pstmt.setString(2, jNode.findPath("date").textValue() + " " + jNode.findPath("time").textValue());
			pstmt.setString(3, jNode.findPath("description").textValue());
		};

		if (executeQuery(sql, sf, null))
			return ok("Event created.");
		else
			return badRequest("Error: " + exceptionMessage);
	}

	public Result updateEvent() {
		DynamicForm requestData = fc.form().bindFromRequest();
		String sql = "UPDATE Events SET location_id = ?, date_time = ?, description = ? WHERE event_id = ?";

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, requestData.get("locationId"));
			pstmt.setString(2, requestData.get("date") + " " + requestData.get("time"));
			pstmt.setString(3, requestData.get("description"));
			pstmt.setString(4, requestData.get("eventId"));
		};

		if (executeQuery(sql, sf, null))
			return ok("Event updated.");
		else
			return badRequest("Error: " + exceptionMessage);
	}

	public Result deleteEvent(int eventId) {
		return ok("Undefined method.");
	}

	public Result selectEvent(int eventId) {
		return ok("Undefined method.");
	}

	public Result selectEventsByFavourite(int userId) {
		return ok("Undefined method.");
	}

	public Result selectEventCloseToPosition(int x, int y) {
		return ok("Undefined method.");
	}

	public Result addEventAttendee(int eventId, int userId) {
		return ok("Undefined method.");
	}

	public Result selectEventAttendees(int eventId) {
		return ok("Undefined method.");
	}

	private boolean executeQuery(String sql, SQLTools.StatementFiller sf, SQLTools.ResultSetProcesser rp) {
		if (rp == null) {
			rp = rs -> {
			};
		}

		if (sf == null) {
			sf = pstmt -> {
			};
		}

		try {
			SQLTools.doPreparedStatement(db, sql, sf, rp);
		} catch (SQLException e) {
			exceptionMessage = e.toString();
			return false;
		}

		return true;
	}
	
	public Result postTest() {
		

//def Secured[A](username: String, password: String)(action: Action[A]) = Action(action.parser) { request =>
//  request.headers.get("Authorization").flatMap { authorization =>
//    authorization.split(" ").drop(1).headOption.filter { encoded =>
//      new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
//        case u :: p :: Nil if u == username && password == p => true
//        case _ => false
//      }
//    }.map(_ => action(request))
//  }.getOrElse {
//    Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured"""")
//  }
//}
		JsonNode json = request().body().asJson();
		String name = json.findPath("name").textValue();
		
		String authorization = request().headers().get("Authorization").toString();
		
		//DynamicForm requestData = fc.form().bindFromRequest();
		
//		if (authorization != null && authorization.startsWith("Basic")) {
//	        // Authorization: Basic base64credentials
//	        String base64Credentials = authorization.substring("Basic".length()).trim();
//	        String credentials = new String(Base64.getDecoder().decode(base64Credentials),
//	                Charset.forName("UTF-8"));
//	        // credentials = username:password
//	        final String[] values = credentials.split(":",2);
		
		return ok("Request header is: ");
	}
}
