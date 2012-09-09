package controllers;

import play.jobs.Job;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.With;
import controllers.minifymod.MinifyAndGzipResponse;

/**
 * Basic example of minified and gzipped responses. Simply add the Annotation
 * @With(MinifyAndGzipResponse.class) to your controller
 */
@With(MinifyAndGzipResponse.class)
public class Continuation extends Controller {

	public static void index() {
		String text = null;
		Promise<String> promise = new Job<String>() {
			@Override
			public String doJobWithResult() throws Exception {
				Thread.sleep(200);
				return "This site is minified and gzipped. It tests running minifymod with async jobs";
			}
		}.now();
		text = await(promise);
		render("index.html", text);
	}

}
