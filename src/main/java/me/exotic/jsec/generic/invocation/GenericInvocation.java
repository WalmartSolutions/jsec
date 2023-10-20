package me.exotic.jsec.generic.invocation;

import me.exotic.jsec.annotations.Runnable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author native
 * Showcases generic invocation techniques.
 */

public class GenericInvocation {

	@Runnable
	public static void run(InvocationType invocationType) {
		if (invocationType.equals(InvocationType.REFLECTION))
			reflection();
		else methodHandles();
	}

	private static void reflection() {
		System.out.println("Type -> reflection");
		// Reflection is the classic way to reflectively access class members.
		// It still has its uses, but for most cases, you'd probably want to use method handles instead

		try {
			// Invoking a method

			// This is the class where the method is **DEFINED**.
			Class<?> clazz = System.class;

			// Locating the method
			// Assuming we want to call exit(int)
			// getDeclaredMethod also gives us method references to protected and private methods. getMethod only returns methods that are public.
			// We give getDeclaredMethod the method name and parameters, and get a method reference or a NoSuchMethodException
			Method method = clazz.getDeclaredMethod("exit", int.class);
			// If this code went through, we can now invoke the method
			// The first argument of the .invoke method is the object to call this method on.
			// This is redundant for static methods, and has to be null. For nonstatic methods, this has to be an object
			//  which extends the class the method was defined on.
			// Since exit is static, we can just pass in null.
			method.invoke(null, 9);
		} catch (Throwable ignored) {
		}
	}

	private static void methodHandles() {
		System.out.println("Type -> handles");
		// Method handles are way more than just ways to invoke methods
		// method handles are basically just programmable data sinks you can configure to go somewhere
		// they're also faster than reflection

		try {
			// Same procedure from before, we need the class where the method is DEFINED
			Class<?> sysClass = System.class;

			// Now, we need to obtain a lookup, to bind a method handle to an existing method or field

			// This is the most basic way to get a lookup. This will use this class as the owner of the lookup,
			//  and allows us to access anything we can normally access from this class via method handles
			MethodHandles.Lookup lookup = MethodHandles.lookup();

			// A public lookup can only access public members, regardless where they are.
			// Its owning class is Object
			@SuppressWarnings("unused")
			MethodHandles.Lookup pubLookup = MethodHandles.publicLookup();

			// The MethodType of System.exit, it returns nothing (void) and accepts an int
			MethodType methodType = MethodType.methodType(void.class, int.class);

			// Next, find the static method "exit" with the given type defined above
			MethodHandle handle = lookup.findStatic(sysClass, "exit", methodType);

			// And finally, invoke it with our arguments
			// Note that, in this case, we do not need to explicitly pass in "null" as instance
			// static method handles do not have an instance argument.
			handle.invoke(9);

			// A quirk of method handles:
			// If the method to be invoked has a varargs parameter (Type... args), the method handle will mimic that behavior.
			// This means, building a method handle to abc(String a, String[] b) will produce a method handle that takes one string and one string array
			//             building a method handle to abc(String a, String... b) will produce a method handle that takes between *one* and *infinite* strings
			// To prevent this, you can map the vararg-capable method handle to a regular one which accepts a string array instead with .asFixedArity():
			MethodHandle varargsHandle = MethodHandles.lookup()
					.findStatic(Arrays.class, "toString", MethodType.methodType(String.class, Object[].class))
					.asVarargsCollector(Object[].class);
			String withVarargs = (String) varargsHandle.invoke("Hello", "world", "abcd");
			String wrongWithVarargs = (String) varargsHandle.invoke((Object) new String[]{"Hello", "world", "abcd"});
			String withoutVarargs = (String) varargsHandle.asFixedArity().invoke((Object) new String[]{"Hello", "world", "abcd"});
			//@formatter:off
            System.out.println(withVarargs); // [Hello, world, abcd]
            System.out.println(wrongWithVarargs); // !!!  [[Ljava.lang.String;@77459877]
                                                  // the String array got passed as element in the argument Object array
            System.out.println(withoutVarargs); // [Hello, world, abcd]
            //@formatter:on
		} catch (Throwable ignored) {
		}
	}
}
