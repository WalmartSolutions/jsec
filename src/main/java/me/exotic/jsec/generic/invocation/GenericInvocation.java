package me.exotic.jsec.generic.invocation;

import me.exotic.jsec.annotations.Runnable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

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
        /*
        The first way to invoke a method and the most common is to use reflection.
        However, it's slow and there is a better way, but I will show how to do it anyway.
         */

        try {
            /*
            First we need to get the class where the method is.
            */
            Class<?> clazz = Class.forName("java.lang.System");

            /*
            Next we can get the method from the class, as well as we need to pass in the parameters of the method.
            In this case exit() takes in an int which is the exit code meaning we need to specify it.
            For specifying others it's the same:
            void.class
            int.class
            long.class
             */
            Method method = clazz.getDeclaredMethod("exit", int.class);
            /*
            And that's it we can now invoke the method.
            You might notice I passed in two parameters even though exit() requires only one.
            The reason for this is that reflection invocation needs an object where it's invoked from.
            You don't need to specify it and can just set it as null.
            The next parameter I pass in is our exit code.
             */
            method.invoke(null, 9);
        } catch (Throwable ignored) {}
    }

    private static void methodHandles() {
        System.out.println("Type -> handles");
        /*
        The next way is using MethodHandles.
        This way is superior and faster this is mainly why you see many obfuscators use it.
        However, it's a little harder to do than with reflection but is still pretty easy once you get it.
         */

        try {
            /*
            First we need to define the class we get the method handle from.
             */
            Class<?> sysClass = Class.forName("java.lang.System");

            /*
            Then we need to obtain a Lookup object to be able to find the method handle in the class.
            Now you might notice that there are a few lookup methods in MethodHandles, the reason for this is mainly because of access.
             */

            /*
            Here is an example of how to get a Lookup object with the lookup() method.
            This method is basically universal and isn't affected by access restrictions.
            It's the most common way to do it.
             */
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            /*
            The next way is using publicLookup() which IS affected by access restrictions.
            As the name implies it gives us public access to the class.
            Meaning we will only see methods that are public.
             */
            @SuppressWarnings("unused")
            MethodHandles.Lookup pubLookup = MethodHandles.publicLookup();

            /*
            Next we need to get define a MethodType for the method we want to invoke.
            The first parameter is well the method type, the next parameter is the method's parameters, which in this case is the exit code.
             */
            MethodType methodType = MethodType.methodType(void.class, int.class);

            /*
            Now it's time to combine all of that together to find the method and then invoke it.
            We pass in the class, the method name, and the method type.
             */
            MethodHandle handle = lookup.findStatic(sysClass, "exit", methodType);

            /*
            The invoking is similar to reflection but doesn't require an object meaning we can straight up pass in the params,
             */
            handle.invoke(9);

        } catch (Throwable ignored) {}
    }
}
