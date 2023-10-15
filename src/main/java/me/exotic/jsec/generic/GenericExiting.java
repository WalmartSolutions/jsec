package me.exotic.jsec.generic;

import me.exotic.jsec.annotations.Runnable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author native
 * Showcases generic exiting techniques.
 */

public class GenericExiting {

	@Runnable
	public static void exiting(int code) {
        /*
        Applications that rely on the JVM to exit if the authentication fails are dumb, It's not the way it should be done.
        One good idea would be to do server-side authentication and not rely on this, but I will showcase it anyway.
         */

        /*
        First we attempt the conventional exiting methods that most people use, no explanation needed here.
         */
		System.exit(code);
		Runtime.getRuntime().exit(code);

        /*
        Next we try exiting with java.lang.Shutdown using reflection to load it.
        For some more backstory, Shutdown is an internal class that by default cannot be used.
        Many of the classes typically used to exit, use the Shutdown class, for example System->exit, Runtime->exit, etc.
        Basically if you hook java.lang.Shutdown's exit() method you will prevent all of the above.
         */
		try {
			Class<?> shutdown = Class.forName("java.lang.Shutdown");

			Method exit = shutdown.getDeclaredMethod("exit", int.class);
			exit.setAccessible(true);

			exit.invoke(null, code);
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
				 IllegalAccessException ignored) {
		}

        /*
        This is only as an example, but you can prevent this as well (by hooking), it's just a little harder than the others.
        The way this works is basically causing a complete segmentation fault in the JVM.
        Remember the fact this still produces a "stacktrace" in the dump.
         */
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);

			Unsafe unsafe = (Unsafe) theUnsafe.get(null);
			unsafe.putAddress(0, 0);
		} catch (NoSuchFieldException | IllegalAccessException ignored) {
		}

        /*
        This is probably the best way of doing it as from what I believe there is no way to prevent it.
        The way this works is by throwing an exception that produces no stacktrace.
         */
		throw new EmptyException();
	}

	public static class EmptyException extends RuntimeException {
		public EmptyException() {
			setStackTrace(new StackTraceElement[0]);
		}
	}
}
