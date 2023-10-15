package me.exotic.jsec.generic;

import me.exotic.jsec.annotations.Runnable;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * @author native
 * Showcases generic launch argument checks.
 */

public class GenericLaunchArgs {
    public static String[] badArgs = {
            // Add more here but this is just an example.
            "-javaagent", "-noverify", "-agentlib"
    };

    @Runnable
    public static void launchArguments() {
        /*
        This takes a little more playing around with it to prevent but is still pretty simple.
        You can hook the ManagementFactory's getInputArguments() method to return no arguments, and you basically win.
         */
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        args.forEach(arg -> {
            for (String badArg : badArgs) {
                if (arg.startsWith(badArg)) {
                    System.out.println("Bad arguments found:");
                    System.out.println(arg);
                }
            }
        });
    }
}
