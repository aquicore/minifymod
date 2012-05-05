package controllers.minifymod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import play.Play;
import play.mvc.Controller;
import play.mvc.Finally;

public class GzipResponse extends Controller {
	
	private static boolean moduleEnabled = true;
	private static boolean gzipEnabled = true;

	static {
		moduleEnabled = !"false".equals(Play.configuration.getProperty("minifymod.moduleEnabled"));
		gzipEnabled = !"false".equals(Play.configuration.getProperty("minifymod.gzipEnabled"));
		if(Play.mode.isDev() && moduleEnabled) {
			moduleEnabled = !"true".equals(Play.configuration.getProperty("minifymod.moduleDisabledOnDev"));
		}
	}
	
	@Finally
	static void compress() throws IOException {
		if(moduleEnabled) {
			// gzip response if enabled, supported and not excluded
			if(gzipEnabled && isGzipSupported() && !isExcluded()) {
				// get rendered content
				String content = response.out.toString();
				// change to a gzipped stream
				final ByteArrayOutputStream gzip = Compression.getGzipStream(content);
				// set response header
				response.setHeader("Content-Encoding", "gzip");
				response.setHeader("Content-Length", gzip.size() + "");
				response.out = gzip;
			}
		}
	}

	public static boolean isGzipSupported() {
		return Compression.isGzipSupported(request);
	}
	
	public static boolean isExcluded() {
		return Compression.isExcludedAction(request);
	}
	
}
