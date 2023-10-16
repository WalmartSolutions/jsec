package me.exotic.jsec.generic;

import me.exotic.jsec.Util;
import sun.misc.Unsafe;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

/**
 * @author native
 * Showcases generic exiting techniques.
 */

public class GenericExiting {

	//	@Runnable
	public static void exit(int code) throws Throwable {
		// Relying on the JVM to exit if authentication fails is a very bad way of going about it.
		// Instead, the client should only ever get to the required information to continue running if authentication succeeded.
		// Authentication without a proper server will not work.

		// This is the most common way to exit a java application.
		// Note: System.exit just calls Runtime.exit.
		System.exit(code);
		Runtime.getRuntime().exit(code);

		// If we want to call Shutdown.exit(int) directly, we need reflection fuckshit.
		// Shutdown is an internal class, behind Runtime.exit.
		MethodHandles.Lookup lookup = Util.obtainAllMightyLookup();
		Class<?> shutdownClass = lookup.findClass("java.lang.Shutdown");
		MethodHandle exitMethod = lookup.findStatic(shutdownClass, "exit", MethodType.methodType(void.class, int.class));

		exitMethod.invoke(code);


		// Crashing the JVM
		// Using unsafe to access out of bounds memory will crash the JVM with a segfault.
		// This normally can't be generally prevented, but it can be avoided by hooking Unsafe to ignore the call if the arguments are invalid like this.
		// It also will produce a stacktrace and a LOT of debug information, in the form of a VM dump (hs_err_<number>.log)
		// Generally not recommended
		Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);

		Unsafe unsafe = (Unsafe) theUnsafe.get(null);
		unsafe.putAddress(0, 0);

		// Assuming the attacker has no access to the direct bytecode of the method, throwing an exception is the best way to terminate the method
		// This cannot be prevented unless you remove the athrow instruction completely (which the attacker cannot do in this case).
		// Since it's a core part of the java language, exceptions can't just be ignored.
		throw new EmptyException();
	}

	public static class EmptyException extends RuntimeException {
		public EmptyException() {
			setStackTrace(new StackTraceElement[0]);
		}

		@Override
		public void printStackTrace(PrintStream s) {

		}

		@Override
		public void printStackTrace(PrintWriter s) {

		}
	}
}
