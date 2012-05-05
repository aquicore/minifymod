package controllers.minifymod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import play.Play;
import play.mvc.Controller;
import play.mvc.Finally;

public class MinifyAndGzipResponse extends Controller {
	
	private static boolean moduleEnabled = true;
	private static boolean minifyEnabled = true;
	private static boolean gzipEnabled = true;

	static {
		moduleEnabled = !"false".equals(Play.configuration.getProperty("minifymod.moduleEnabled"));
		minifyEnabled = !"false".equals(Play.configuration.getProperty("minifymod.minifyEnabled"));
		gzipEnabled = !"false".equals(Play.configuration.getProperty("minifymod.gzipEnabled"));
		if(Play.mode.isDev() && moduleEnabled) {
			moduleEnabled = !"true".equals(Play.configuration.getProperty("minifymod.moduleDisabledOnDev"));
		}
	}
	
	@Finally
	static void compress() throws IOException {
		if(moduleEnabled) {
			// get rendered content
			String content = response.out.toString();
			// minify
			if(minifyEnabled && !isExcluded()) {
				// select compression method by contentType
				if (response.contentType.contains("text/html")) {	// could be "text/html; charset=utf-8"
					content = Compression.compressHTML(content);
				} else if (response.contentType.contains("text/xml")) {
					content = Compression.compressXML(content);
				} else if (response.contentType.contains("text/css")) {
					content = Compression.compressCSS(content);
				} else if (response.contentType.contains("text/javascript")) {
					content = Compression.compressJS(content);
				}
			}

			// gzip only if enabled, supported and not excluded
			if(gzipEnabled && isGzipSupported() && !isExcluded()) {
				final ByteArrayOutputStream gzip = Compression.getGzipStream(content);
				// set response header
				response.setHeader("Content-Encoding", "gzip");
				response.setHeader("Content-Length", gzip.size() + "");
				response.out = gzip;
			} else {
				response.out = new ByteArrayOutputStream(content.length());
				response.out.write(content.getBytes());
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
