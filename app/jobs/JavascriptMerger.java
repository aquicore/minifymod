/**
 * @author maklemenz
 */
package jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.Play;
import play.Play.Mode;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.vfs.VirtualFile;
import controllers.minifymod.Compression;

// pretty simple job to merge and compress different Javascripts to one
@OnApplicationStart(async = false)
public class JavascriptMerger extends Job {

	@Override
	public void doJob() throws Exception {
		boolean active = Play.configuration.getProperty("javascriptmerger.active", "false").toLowerCase()
				.equals("true");
		boolean compression = Play.configuration.getProperty("javascriptmerger.compression", "true").toLowerCase()
				.equals("true");
		if (!active || Play.mode == Mode.PROD) {
			Logger.debug("Job " + this.getClass().getSimpleName() + " disabled");
			return;
		}

		Logger.debug("Job " + this.getClass().getSimpleName() + " started");

		String basePath = Play.applicationPath + File.separator;

		String defaultConfFileName = "conf" + File.separator + "javascriptmerger.conf";
		String defaultJSFileName = "public" + File.separator + "javascripts" + File.separator + "merged.min.js";
		String confFileName = Play.configuration.getProperty("javascriptmerger.conffile", defaultConfFileName);
		String jsFileName = Play.configuration.getProperty("javascriptmerger.jsfile", defaultJSFileName);

		File confFile = new File(basePath + confFileName);
		File jsFile = new File(basePath + jsFileName);
		StringBuilder out = new StringBuilder(4096);
		List<String> scripts = FileUtils.readLines(confFile);
		for (String script : scripts) {
			// comment-char is '#'
			if (script.startsWith("#")) {
				continue;
			}
			if (!handleJavascript(script, out, compression)) {
				throw new FileNotFoundException("script " + script + " was not found!");
			}

		}

		FileWriter outFile = new FileWriter(jsFile);
		String mergedJs = out.toString();
		outFile.write(mergedJs);
		outFile.close();
		Logger.debug("length of mergedJs-file is %s", mergedJs.length());
		Logger.debug("merged js written to %s", jsFile.getAbsolutePath());
		Logger.debug("Job " + this.getClass().getSimpleName() + " ended");
	}

	private boolean handleJavascript(String script, StringBuilder out, boolean compression) throws Exception {
		Logger.debug("handling script: " + script);

		Map<String, VirtualFile> modules = Play.modules;
		modules.put("application itself", VirtualFile.fromRelativePath(""));
		for (VirtualFile f : modules.values()) {
			if (f.getRealFile().canRead()) {
				File pub = new File(f.getRealFile(), "public/javascripts");
				File scriptFile = new File(pub, script);
				if (scriptFile.exists()) {
					String scriptContent = FileUtils.readFileToString(scriptFile);
					if (compression) {
						scriptContent = Compression.compressJS(scriptContent);
					}
					out.append(scriptContent).append(";\n");
					return true;
				}
			}
		}
		return false;
	}

}
