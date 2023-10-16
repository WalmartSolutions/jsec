package me.exotic.jsec.breakdowns.zzurio;


import me.exotic.jsec.generic.GenericExiting;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * @author native
 * Breakdown of zzurio's "Anti-Dump".
 * <p>
 * Anti-dump techniques mainly try to prevent the external dumping of classes. That is, running the program and asking the JVM for all loaded classes.
 * This bypasses all the obscuring techniques employed by all loaders. As long as the loader can actually load classes, they can be dumped.
 * <p>
 * This anti dump is a bit primitive, in the sense that it just checks for "bad" flags present in the launch args.
 */
public class AntiDump {

	/**
	 * "naughty" flags that often indicate someone is doing something nasty
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
	 * Exits the JVM forcefully, with exit code 0.
	 * Just serves as a dummy link to the techniques presented in {@link GenericExiting}
	 *
	 * @throws Throwable
	 */
	private static void dumpDetected() throws Throwable {
		GenericExiting.exit(0);

		// The original method used by the source essentially uses the same techniques as GenericExiting:
//		private static void dumpDetected() {
//			try {
//				unsafe.putAddress(0, 0);
//			} catch (Exception e) {}
//			FMLCommonHandler.instance().exitJava(0, false); // Shutdown.
//			Error error = new Error();
//			error.setStackTrace(new StackTraceElement[]{});
//			throw error;
//		}
	}

	/**
	 * Actually checks the arguments present on the JVM
	 */
	public void argumentCheck() {
		/*
		  Pulled from <a href="https://github.com/zzurio/Anti-Dump/blob/main/AntiDump.java"></a>
		  Right off the bat we see usage of {@link sun.management.VMManagement} to get JVM arguments.
		  Problem is the fact that this can be hooked just like {@link me.exotic.jsec.generic.GenericLaunchArgs}.
		  After hooking it we bypassed the JVM argument checks and can now freely attach an agent.
		 */
		List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

		/*
		  Pulled from <a href="https://github.com/zzurio/Anti-Dump/blob/main/AntiDump.java"></a>
		  This checks if the Instrumentation class is already loaded.
		  If it is, this will throw an exception, since we're redefining an already existing class.
		  If not, this will disable instrumentation from working in the future as long as the jvm is running,
		  since the instrumentation class is now empty.
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

		/*
		  Another way to compromise it is the same as in {@link HWIDAuth}.
		  By simply preventing a call to AntiDump.check().
		 */
	}
}
