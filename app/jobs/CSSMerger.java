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

// pretty simple job to merge and compress different Stylesheets to one
@OnApplicationStart(async = false)
public class CSSMerger extends Job {

	@Override
	public void doJob() throws Exception {
		boolean active = Play.configuration.getProperty("cssmerger.active", "false").toLowerCase().equals("true");
		boolean compression = Play.configuration.getProperty("cssmerger.compression", "true").toLowerCase()
				.equals("true");
		if (!active || Play.mode == Mode.PROD) {
			Logger.debug("Job " + this.getClass().getSimpleName() + " disabled");
			return;
		}

		Logger.debug("Job " + this.getClass().getSimpleName() + " started");

		String basePath = Play.applicationPath + File.separator;

		String defaultConfFileName = "conf" + File.separator + "cssmerger.conf";
		String defaultCSSFileName = "public" + File.separator + "stylesheets" + File.separator + "merged.min.css";
		String confFileName = Play.configuration.getProperty("cssmerger.conffile", defaultConfFileName);
		String cssFileName = Play.configuration.getProperty("cssmerger.cssfile", defaultCSSFileName);

		File confFile = new File(basePath + confFileName);
		File cssFile = new File(basePath + cssFileName);
		StringBuilder out = new StringBuilder(4096);
		List<String> sheets = FileUtils.readLines(confFile);
		for (String sheet : sheets) {
			// comment-char is '#'
			if (sheet.startsWith("#")) {
				continue;
			}
			if (!handleStylesheet(sheet, out, compression)) {
				throw new FileNotFoundException("stylesheet " + sheet + " was not found!");
			}

		}

		FileWriter outFile = new FileWriter(cssFile);
		String mergedCss = out.toString();
		outFile.write(mergedCss);
		outFile.close();
		Logger.debug("length of mergedCss-file is %s", mergedCss.length());
		Logger.debug("merged css written to %s", cssFile.getAbsolutePath());
		Logger.debug("Job " + this.getClass().getSimpleName() + " ended");
	}

	private boolean handleStylesheet(String sheet, StringBuilder out, boolean compression) throws Exception {
		Logger.debug("handling sheet: " + sheet);

		Map<String, VirtualFile> modules = Play.modules;
		modules.put("application itself", VirtualFile.fromRelativePath(""));
		for (VirtualFile f : modules.values()) {
			if (f.getRealFile().canRead()) {
				File pub = new File(f.getRealFile(), "public/stylesheets");
				File cssFile = new File(pub, sheet);
				if (cssFile.exists()) {
					String sheetContent = FileUtils.readFileToString(cssFile);
					if (compression) {
						sheetContent = Compression.compressCSS(sheetContent);
					}
					out.append(sheetContent).append(";\n");
					return true;
				}
			}
		}
		return false;
	}

}
