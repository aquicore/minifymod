package controllers;

import play.mvc.Controller;
import play.mvc.With;
import controllers.minifymod.MinifyAndGzipResponse;

/**
 * Basic example of minified and gzipped responses. Simply add the Annotation
 * @With(MinifyAndGzipResponse.class) to your controller
 */
@With(MinifyAndGzipResponse.class)
public class MinifiedAndGzipped extends Controller {

	public static void index() {
		String text = "This site is minified and gzipped (if your browser supports it)";
		render("index.html", text);
	}

}
