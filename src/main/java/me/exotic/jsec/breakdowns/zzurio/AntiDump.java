package me.exotic.jsec.breakdowns.zzurio;



import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author native
 * Breakdown of zzurio's "Anti-Dump".
 * WARNING: If obfuscated this may not be the same.
 */
public class AntiDump {
    /**
     * Anti-Dump is meant to prevent dumping loaded classes.
     * The way it works is by implementing checks for instrumentation dumpers.
     * But can be compromised by other dumpers like hooked ones that dump classes passed through the classLoader.
     * A few techniques it uses are argument checking, disabling instrumentation, etc.
     * Let's jump in and see what we can figure out.
     */

    /**
     * First thing we see is a {@link java.lang.String} array called "naughtyFlags".
     * It contains a bunch of JVM arguments.
     */
    private static final String[] naughtyFlags = {
            "-XBootclasspath",
            "-javaagent",
            "-Xdebug",
            "-agentlib",
            "-Xrunjdwp",
            "-Xnoagent",
            "-verbose",
            "-DproxySet",
            "-DproxyHost",
            "-DproxyPort",
            "-Djavax.net.ssl.trustStore",
            "-Djavax.net.ssl.trustStorePassword"
    };

    /**
     * Then we move on to the argument check.
     */
    public void argumentCheck() {
        /**
         * Pulled from <a href="https://github.com/zzurio/Anti-Dump/blob/main/AntiDump.java"></a>
         * Right off the bat we see usage of {@link sun.management.VMManagement} to get JVM arguments.
         * Problem is the fact that this can be hooked just like {@link me.exotic.jsec.generic.GenericLaunchArgs}.
         * After hooking it we bypassed the JVM argument checks and can now freely attach an agent.
         */
        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

        /**
         * Pulled from <a href="https://github.com/zzurio/Anti-Dump/blob/main/AntiDump.java"></a>
         * Now we can see an attempt at disabling {@link java.lang.instrument.Instrumentation}.
         * This is done to prevent agents from working.
         * However, a way of finding if this happens is by hooking Unsafe->defineClass to get the name of each defined class.
         * I won't share how to disable this, you will have to figure that out yourself.
         */

        /*
        try {
            byte[] bytes = createDummyClass("java/lang/instrument/Instrumentation");
            unsafe.defineClass("java.lang.instrument.Instrumentation", bytes, 0, bytes.length, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
            dumpDetected();
        }
         */

        /**
         * Next is the usage of {@link sun.misc.Unsafe} to cause a segmentation fault for the JVM.
         * {@link me.exotic.jsec.generic.GenericExiting} contains an explanation of this.
         * However, this is bad because the whole method can simply be nop'ed to do nothing.
         * Which would essentially compromise the whole Anti-Dump.
         */

        /*
        private static void dumpDetected() {
            try {
                unsafe.putAddress(0, 0);
            } catch (Exception e) {}
            FMLCommonHandler.instance().exitJava(0, false); // Shutdown.
            Error error = new Error();
            error.setStackTrace(new StackTraceElement[]{});
            throw error;
        }
         */

        /**
         * Another way to compromise it is the same as in {@link me.exotic.jsec.breakdowns.zzurio.HWIDAuth}.
         * By simply preventing a call to AntiDump.check().
         */
    }
}
