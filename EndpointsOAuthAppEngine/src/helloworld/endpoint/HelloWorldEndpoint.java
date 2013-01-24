package helloworld.endpoint;

import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.users.User;

@Api(name = "helloworld", version = "v1", clientIds = {
		ClientIds.WEB_CLIENT_ID, ClientIds.ANDROID_CLIENT_ID,
		ClientIds.ANDROID_DEBUG_CLIENT_ID }, audiences = { ClientIds.AUDIENCE },
		scopes = {
		"https://www.googleapis.com/auth/userinfo.email",
		"https://www.googleapis.com/auth/userinfo.profile" })
public class HelloWorldEndpoint {

	private static final Logger log = Logger.getLogger(HelloWorldEndpoint.class
			.getName());

	@ApiMethod(name = "scores.get")
	public HelloWorldEntity get(@Named("id") String id, User user) {
		HelloWorldEntity result = new HelloWorldEntity();
		log.warning("user : " + user);
		result.setResult("KO");
		if (user != null) {
			log.warning("Authentication OK : " + user.getEmail());
			result.setResult("OK: " + user.getEmail());
		}
		return result;
	}
}
