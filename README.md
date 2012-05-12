Minifymod - Response optimization for Play

Since Play 1.2.x has no build in support for response optimization like minify and gzip responses this module adds this functionality using the YUI Compressor and googles htmlcompressor.

The latest version of this module can be fetched from github : https://github.com/maklemenz/play-minifymod

How To Use It

Let’s say you have a simple controller wich delivers a hugh site:

	public class MustBeOptimized extends Controller {
		public static void renderMyHughTemplate() {
			render("largeSite.html");
		}
	}

Minify the response

If you want it to get delivered minimized for example by removing unnecessary whitespaces, you just have to add a single line

	@With(MinifyResponse.class)
	public class IsABitOptimized extends Controller {
		public static void renderMyHughTemplate() {
			render("largeSite.html");
		}
	}

Gzip the response

If you want it to get delivered gezipped, you just have to add another single line:

	@With(GzipResponse.class)
	public class ABitMoreOptimized extends Controller {
		public static void renderMyHughTemplate() {
			render("largeSite.html");
		}
	}

Minify and gzip the response

However, response size will be reduced most by doing both optimizations:

	@With(MinifyAndGzipResponse.class)
	public class Optimized extends Controller {
		public static void renderMyHughTemplate() {
			render("largeSite.html");
		}
	}

Exclusions

Sometimes it is necessary to exclude one or two methods which for example delivers images or binarys. This can be done in two ways:

  -call Compression.excludeAction(request.action); in this action which is the same as “Controller.method” wich is the most fast and easy way. In fact it puts a variable in the renderArgs
  -extend one of the tree classes (or all) and override isExcluded() which can be the most performant way since if it returns false all the way, it won’t check the renderArgs for a specific value for each request

Some extras for you

Since you probably want to use caching in your application, you can use the methods of the Compression.class like compressHtml(String html) or compressCss(String css), ..js, ..xml
Configuration

The configuration is pretty self explaining. This are the keys. They are all in their default state, so it is not necessary to include this in your application.conf if you don’t want to change the settings

	minifymod.moduleEnabled = true
	minifymod.moduleDisabledOnDev = false
	minifymod.minifyEnabled = true
	minifymod.gzipEnabled = true
	
Planned features

Just leave a comment and I will see what I can do