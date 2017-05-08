package controllers.tools;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;

public class SecuredAction extends Action.Simple {
	public CompletionStage<Result> call(Http.Context ctx) {
		// String token = getTokenFromHeader(ctx);

		return CompletableFuture.completedFuture(badRequest("Secured Action says Not authorized."));
		// Logger.info("Calling action for {}", ctx);
		// return delegate.call(ctx);
	}
}

//
// public F.Promise<Result> call(Http.Context ctx) throws Throwable {
// String token = getTokenFromHeader(ctx);
// if (token != null) {
// User user = User.find.where().eq("authToken", token).findUnique();
// if (user != null) {
// ctx.request().setUsername(user.username);
// return delegate.call(ctx);
// }
// }
// Result unauthorized = Results.unauthorized("unauthorized");
// return F.Promise.pure(unauthorized);
// }
//
// private String getTokenFromHeader(Http.Context ctx) {
// String[] authTokenHeaderValues = ctx.request().headers().get("X-AUTH-TOKEN");
// if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) &&
// (authTokenHeaderValues[0] != null)) {
// return authTokenHeaderValues[0];
// }
// return null;
// }
//
