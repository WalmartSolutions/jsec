package me.exotic.jsec;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class Util {
	/**
	 * Gets an allmighty lookup, capable of accessing internal classes of the jsl.
	 *
	 * @return The lookup
	 * @throws NoSuchFieldException   Never happens
	 * @throws IllegalAccessException Should never happen
	 */
	public static MethodHandles.Lookup obtainAllMightyLookup() throws NoSuchFieldException, IllegalAccessException {
		// This is the trusted lookup. It can do everything, that's why we use it to trick jigsaw
		Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");

		// Standard unsafe procedure
		Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Unsafe unsafe = (Unsafe) theUnsafe.get(null);

		// Get the object base for the IMPL_LOOKUP field, and the offset
		long fieldOffset = unsafe.staticFieldOffset(implLookup);
		Object objectBase = unsafe.staticFieldBase(implLookup);
		// and use those to get the IMPL_LOOKUP lookup without jigsaw fucking us over
		return (MethodHandles.Lookup) unsafe.getObject(objectBase, fieldOffset);
	}
}
