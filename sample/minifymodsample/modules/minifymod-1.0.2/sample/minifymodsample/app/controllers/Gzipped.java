package controllers;

import play.mvc.Controller;
import play.mvc.With;
import controllers.minifymod.GzipResponse;

/**
 * Basic usage of gzipped outputstreams. By adding the Annotation
 * @With(GzipResponse.class) to your controller every single response will be
 * delivered gezipped if the client supports it
 */
@With(GzipResponse.class)
public class Gzipped extends Controller {

	public static void index() {
		String text = "This site is gzipped (if your browser supports it)";
		render("index.html", text);
	}

}
