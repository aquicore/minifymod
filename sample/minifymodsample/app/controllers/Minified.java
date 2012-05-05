package controllers;

import play.mvc.Controller;
import play.mvc.With;
import controllers.minifymod.MinifyResponse;

/**
 * Basic example of minified responses if any controller extends
 * MinifyResponse.class the responses get minified. Works for HTML, CSS,
 * Javascript, XML and full templates
 */
@With(MinifyResponse.class)
public class Minified extends Controller {

	public static void index() {
		String text = "This site is minified";
		render("index.html", text);
	}

}
