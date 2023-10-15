package me.exotic.jsec.generic;

import me.exotic.jsec.annotations.Runnable;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author native
 * Showcases generic launch argument checks.
 */

public class GenericLaunchArgs {
	private static final Pattern badArgDetector = Pattern.compile("^-(javaagent|noverify|agentlib).+$");

	@Runnable
	public static void launchArguments() {
        /*
        This takes a little more playing around with it to prevent but is still pretty simple.
        You can hook the ManagementFactory's getInputArguments() method to return no arguments, and you basically win.
         */
		List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
		for (String arg : args) {
			Matcher m = badArgDetector.matcher(arg);
			if (m.matches()) {
				System.out.println("Bad argument: " + arg);
			}
		}
	}
}
