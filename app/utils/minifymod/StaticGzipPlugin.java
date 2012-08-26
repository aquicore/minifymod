package utils.minifymod;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CACHE_CONTROL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.ETAG;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.LAST_MODIFIED;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import play.Logger;
import play.Play;
import play.Play.Mode;
import play.PlayPlugin;
import play.cache.Cache;
import play.libs.MimeTypes;
import play.mvc.Http;
import play.utils.Utils;
import play.vfs.VirtualFile;
import controllers.minifymod.Compression;

/**
 * Based on https://gist.github.com/2882360
 *
 */
public class StaticGzipPlugin extends PlayPlugin {

	@Override
	public boolean serveStatic(VirtualFile file, Http.Request request,
			Http.Response response) {
		try {
			final File localFile = file.getRealFile();
			String contentType = MimeTypes.getContentType(localFile.getName(),
					"text/plain");
			// You don't want to gzip images ;-)
			if (contentType.contains("image")) {
				return false;
			}
			response.setContentTypeIfNotSet(contentType);
			response = addEtag(request, response, localFile);
			// minify
			String key = request.path + localFile.toString();
			String content = Cache.get(key, String.class);
			if (content == null) {
				content = minify(request, response, localFile);
				if (Play.mode == Mode.PROD) {
					Cache.set(key, content, "24h");
				}
			}

			// gzip only if supported and not excluded
			if (Compression.isGzipSupported(request)
					&& !Compression.isExcludedAction(request)) {
				final ByteArrayOutputStream gzip = Compression
						.getGzipStream(content);
				// set response header
				response.setHeader("Content-Encoding", "gzip");
				response.setHeader("Content-Length", gzip.size() + "");
				response.out = gzip;
				return true;
			} else {
				response.out = new ByteArrayOutputStream(content.length());
				response.out.write(content.getBytes());
				return true;
			}
		} catch (Exception e) {
			Logger.error(e, "Error when Gzipping response: %s", e.getMessage());

		}
		return false;
	}

	private String minify(Http.Request request, Http.Response response,
			File file) throws IOException {
		boolean minified = file.getName().contains(".min.");
		String content = VirtualFile.open(file).contentAsString();
		if (minified) {
			return content;
		} else if (!Compression.isExcludedAction(request)) {
			// select compression method by contentType
			if (response.contentType.contains("text/html")) {
				return Compression.compressHTML(content);
			} else if (response.contentType.contains("text/xml")) {
				return Compression.compressXML(content);
			} else if (response.contentType.contains("text/css")) {
				return Compression.compressCSS(content);
			} else if (response.contentType.contains("text/less")) {
				return Compression.compressCSS(content);
			} else if (response.contentType.contains("text/javascript")
					|| response.contentType.contains("application/javascript")) {
				return Compression.compressJS(content);
			}
		}
		return content;
	}

	private static Http.Response addEtag(Http.Request request,
			Http.Response response, File file) {
		if (Play.mode == Play.Mode.DEV) {
			response.setHeader(CACHE_CONTROL, "no-cache");
		} else {
			String maxAge = Play.configuration.getProperty("http.cacheControl",
					"3600");
			if (maxAge.equals("0")) {
				response.setHeader(CACHE_CONTROL, "no-cache");
			} else {
				response.setHeader(CACHE_CONTROL, "max-age=" + maxAge);
			}
		}
		boolean useEtag = Play.configuration
				.getProperty("http.useETag", "true").equals("true");
		long last = file.lastModified();
		final String etag = "\"" + last + "-" + file.hashCode() + "\"";
		if (!request.isModified(etag, last)) {
			if (request.method.equals("GET")) {
				response.status = Http.StatusCode.NOT_MODIFIED;
			}
			if (useEtag) {
				response.setHeader(ETAG, etag);
			}
		} else {
			response.setHeader(LAST_MODIFIED, Utils.getHttpDateFormatter()
					.format(new Date(last)));
			if (useEtag) {
				response.setHeader(ETAG, etag);
			}
		}
		return response;
	}

}