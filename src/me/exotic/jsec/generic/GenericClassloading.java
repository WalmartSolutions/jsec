package me.exotic.jsec.generic;

import me.exotic.jsec.annotations.Runnable;
import sun.misc.Unsafe;
import java.lang.reflect.Field;

/**
 * @author native
 * Showcases generic class loading techniques.
 * Warning: This might be a little incorrect because it was done at an absurd time.
 * If you find anything wrong with it, open an issue.
 */
public class GenericClassloading {
    /*
    A little disclaimer: this can be hooked so don't use it.
    The best approach in my opinion of loading classes is using JVMTI.
    Learning about JVMTI is out of the range of JSec.
     */
    
    /**
     Showcases how classloading looks using {@link sun.misc.Unsafe}
     */
    public void unsafe() {
        /*
        If you didn't already know sun.misc.Unsafe can be used to load classes as well.
        It is however more complex to do and overall isn't recommended, because it is... well unsafe.
        But here's how to do it anyway.
         */

        // Get an instance of sun.misc.Unsafe, we need to use reflection for this since sun.misc.Unsafe is an internal class.
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");

            // Set the field accessible as it isn't by default and will throw an exception.
            theUnsafe.setAccessible(true);

            Unsafe unsafe = (Unsafe) theUnsafe.get(null);

            // Get the class to load
            Class<?> loadClass = GenericClassloading.class;

            /*
            The reference to loadClass.getName() returns the fully qualified name of loadClass.
            Meaning it would return this: me.exotic.jsec.generic.GenericClassloading.
            For Unsafe you need to get the class bytes of the class to load it.
            For this I suggest using ASM's ClassReader.
             */

            // Here's an example on how to get the classBytes using ASM's ClassReader.
            // ClassReader classReader = new ClassReader(loadClass.getName());

            // Get the classBytes using ASM's ClassReader, this is just an example.
            byte[] classBytes = new byte[0];
            unsafe.defineClass(loadClass.getName(), classBytes, 0, classBytes.length, loadClass.getClassLoader(), null);

            // Since Unsafe->defineClass returns java.lang.Class, we can get an object.
            Class<?> loadedClass = unsafe.defineClass(loadClass.getName(), classBytes, 0, classBytes.length, loadClass.getClassLoader(), null);

            /*
            There you go, you have loaded a class using sun.misc.Unsafe.
             */
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
    }

    /**
     * Showcases "fun stuff" related to classloading.
     */
    @Runnable
    public void misc() {
        /*
        Each class is loaded by a classLoader meaning we can get the classLoader of this class.
         */
        System.out.println(GenericClassloading.class.getClassLoader());

        /**
         * The way classloading works in Java is like this: the JVM requests a class.
         * The classLoader then tries to locate the class the JVM specified.
         * In this case the classLoader is {@link java.lang.ClassLoader}.
         * The class is passed into the classLoader by its full name, meaning it contains the package it is in.
         * If the classLoader can't find the class specified by the JVM it throws {@link java.lang.ClassNotFoundException}.
         * Or in some cases it may be {@link java.lang.NoClassDefFoundError}.
         */
    }

    /**
     Showcases how classloading looks using {@link java.lang.ClassLoader}
     TODO: Make this better and more in depth.
     */
    public void classLoader() {
        try {
            // Create an instance of java.lang.ClassLoader
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();

            // Call java.lang.ClassLoader to load our class.
            classLoader.loadClass("me.exotic.jsec.generic.GenericClassloading");

            // We can obtain the java.lang.Class object like this, since loadClass() returns java.lang.Class.
            Class<?> classLoaderClass = classLoader.loadClass("me.exotic.jsec.generic.GenericClassloading");
        } catch (ClassNotFoundException ignored) {}
    }
}

