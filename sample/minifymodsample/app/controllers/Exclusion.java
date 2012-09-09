package controllers;

import play.mvc.Controller;
import play.mvc.With;
import controllers.minifymod.Compression;
import controllers.minifymod.MinifyAndGzipResponse;


/**
 * Basic usage of exclusions Sometimes you need to disable gzipped and minified
 * responses for a single action
 * 
 */
@With(MinifyAndGzipResponse.class)
public class Exclusion extends Controller {

	public static void index() {
		// exclude this action
		Compression.excludeAction(request.action); // = "Exclusion.index"
		String text = "This site is excluded from response optimization";
		render("index.html", text);
	}

}
