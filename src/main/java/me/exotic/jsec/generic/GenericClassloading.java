package me.exotic.jsec.generic;

import me.exotic.jsec.annotations.Runnable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

/**
 * @author native
 * Showcases generic class loading techniques.
 */
public class GenericClassloading implements Opcodes {

	@Runnable
    public static void run() throws Throwable {
		System.out.println(ManagementFactory.getRuntimeMXBean().getInputArguments());
		new GenericClassloading().defineUsingCustomClassLoader();
	}

	private static byte[] getExampleClass(String name) {
		// The asm part is just to get a class file buffer.
		// You can usually just read a .class file into a byte[] and you will get a valid class file buffer you can use directly
		ClassNode node = new ClassNode();
		// Make a class in the example package, called Hello, extending Object
		node.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", null);
		// make a new public static method in that class, called print, taking one String and returning nothing
		MethodVisitor test = node.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "print", "(Ljava/lang/String;)V", null, null);
		test.visitCode();
		// Load the System.out field
		test.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		// Load the first argument, the String to print
		test.visitVarInsn(ALOAD, 0);
		// Call PrintStream.println(String) on System.out we previously loaded, with the argument String
		test.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		// Return nothing
		test.visitInsn(RETURN);
		test.visitEnd();

		// Turn the class model from ASM into a class buffer
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		node.accept(cw);
		// This class buffer is a valid .class file: you could write it to a file and use it as an actual .class
		return cw.toByteArray();
	}

	/**
	 * Defines a class using a custom class loader.
	 * This is one of the oldest and most used ways to define a class.
	 * It has a few flaws, one major being that the class loader being used is one we made ourselves.
	 *
	 * @throws NoSuchMethodException     Never happens
	 * @throws InvocationTargetException Never happens
	 * @throws IllegalAccessException    Never happens
	 */
	public void defineUsingCustomClassLoader() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// get the class buffer
		byte[] exampleClass = getExampleClass("example/Example");

		// make an instance of our class loader
		HackableClassLoader hcl = new HackableClassLoader(getClass().getClassLoader());

		// tell it to define a class using that buffer
		Class<?> definedClass = hcl.define(exampleClass);

		// Now, we have a reference to our Example class
		System.out.println(definedClass.getName()); // example.Example
		Method printlnMethod = definedClass.getMethod("print", String.class);
		printlnMethod.invoke(null, "Hello world"); // will print Hello world
	}

	/**
	 * Defines a class using the method handle lookup mechanism
	 *
	 * @throws NoSuchMethodException     never happens
	 * @throws IllegalAccessException    MethodHandles.lookup().defineClass
	 * @throws InvocationTargetException never happens
	 */
	public void defineUsingMethodHandles() throws Throwable {
		byte[] exampleClass = getExampleClass("me/exotic/jsec/generic/ExampleClass");

		// Define our class we got earlier with the Lookup on our class.
		// THIS HAS A CRUCIAL FLAW HOWEVER:
		// Classes can only be defined IF THEY SHARE THE SAME PACKAGE NAME AS THE CLASS FROM THE LOOKUP.
		// This means that you can only define classes if you already have a class in the EXACT SAME PACKAGE as the new class you want to define.
		Class<?> definedClass = MethodHandles.lookup().defineClass(exampleClass);
		// Now, we have a reference to our Hello class we defined earlier
		System.out.println(definedClass.getName()); // me.exotic.jsec.generic.ExampleClass
		Method printlnMethod = definedClass.getMethod("print", String.class);
		printlnMethod.invoke(null, "Hello world"); // will print Hello world

		// However, this also has a cool advantage: You can define "hidden" classes.
		// Hidden classes are not discoverable by the JVM during normal linking. That means, you can't directly access it from bytecode.
		//  Code in the hidden class itself is exempt from this rule.
		// Class.forName, ClassLoader.findLoadedClass and Lookup.findClass won't find hidden classes.
		// A hidden class can't be used as superclass, field type, return type or parameter type, as mentioned.
		// Stack traces will not show elements from hidden classes (!!).

		// This method will also directly return a Lookup on the newly defined class.

		// The nestmate option indicates that the class being loaded is a "nest mate" of the class in the lookup.
		// Classes in the same nest can always access private members of each other, regardless of jigsaw status.
		// "strong" classes don't get garbage collected, unless the class loader defining it also gets garbage collected.
		MethodHandles.Lookup hiddenClassLookup = MethodHandles.lookup().defineHiddenClass(getExampleClass("me/exotic/jsec/generic/ExampleClass1"), false, MethodHandles.Lookup.ClassOption.NESTMATE, MethodHandles.Lookup.ClassOption.STRONG);
		Class<?> hiddenClassRef = hiddenClassLookup.lookupClass();
		MethodHandle printMethod = hiddenClassLookup.findStatic(hiddenClassRef, "print", MethodType.methodType(void.class, String.class));
		printMethod.invoke("Hello world"); // Prints hello world
	}

	/**
	 * Defines a class using the Java Lang Access mechanism
	 */
	public void defineUsingJLA() throws Throwable {
		// Get an example class buffer with the example.Hello class
		byte[] classBytes = getExampleClass("example/Hello");

		// Now that we have our class buffer, we can use some hackery to define a class without intervention

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
		MethodHandles.Lookup theImplLookup = (MethodHandles.Lookup) unsafe.getObject(objectBase, fieldOffset);

		// Now that we have an all-mighty lookup, we can use it to do anything, including accessing jdk internals
		// The SharedSecrets class contains a lot of features that access internals of the jdk
		Class<?> sharedSecretsClass = theImplLookup.findClass("jdk.internal.access.SharedSecrets");
		// one of those features is the java lang access, which includes a way for us to define a class into any classloader we want
		Class<?> JLAClass = theImplLookup.findClass("jdk.internal.access.JavaLangAccess");
		MethodHandle getJLA = theImplLookup.findStatic(sharedSecretsClass, "getJavaLangAccess", MethodType.methodType(JLAClass));
		// This is the method to actually define a class from class bytes, into any loader.
		MethodHandle JLADefineClass = theImplLookup.findVirtual(JLAClass, "defineClass",
				MethodType.methodType(Class.class, ClassLoader.class, String.class, byte[].class, ProtectionDomain.class, String.class));
		Object JLAInst = getJLA.invoke();
		// Invoke the defineClass(ClassLoader, String, byte[], ProtectionDomain, String) method
		// The ClassLoader being null denotes that we mean the boot class loader, aka the one managing the startup of the jvm.
		// It itself has some funny quirks that make classes loaded in it a bit more powerful.
		// The second argument is the name of the class. Setting it to null will make the method figure it out itself.
		Class<?> definedClass = (Class<?>) JLADefineClass.invoke(JLAInst, null, null, classBytes, null, null);

		// Now, we have a reference to our Hello class we defined earlier
		System.out.println(definedClass.getName()); // example.Hello
		Method printlnMethod = definedClass.getMethod("print", String.class);
		printlnMethod.invoke(null, "Hello world"); // will print Hello world
	}

	/**
	 * Showcases "fun stuff" related to classloading.
	 */
	@Runnable
	public void misc() {
		// Each class is loaded by a classLoader meaning we can get the classLoader of this class.
		System.out.println(GenericClassloading.class.getClassLoader());

        /*
        Classloading breakdown:
        1. The JVM requests a class
        2. The JVM goes to the class loader of the class requesting a specific class, and asks it for that class
        3. The class loader needs to load the class identified by its binary name, and defines it if it isn't already defined
        4. The class loader either successfully loads the class and defines it, or throws a ClassNotFoundException
         */
	}

	/**
	 * Showcases how classloading looks using {@link ClassLoader}
	 */
	public void classLoader() {
		try {
			// This is the system class loader. It is responsible for loading the JSL.
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();

			// This will usually be the application class loader. It's responsible for loading classes from the user
			ClassLoader appClassLoader = getClass().getClassLoader();

			// We can now use one of the class loaders to load another class, and get a Class reference to it
			Class<?> stringClass = classLoader.loadClass("java.lang.String");
			System.out.println(stringClass.getSimpleName()); // String

			// Or, we can use Class to load it for us:
			// In this case, we can also specify if we want the class to be initialized directly or not.
			// Here, "false" indicates that the JVM will not directly initialize the class (doesn't run <clinit>, does not populate constants)
			// Instead, it is postponed to whenever we invoke a method on this class, or try to access a field.
			Class<?> simpleAuthClass = Class.forName("me.exotic.jsec.breakdowns.SimpleAuth", false, appClassLoader);
			System.out.println(simpleAuthClass.getSimpleName()); // SimpleAuth
			Constructor<?> constructor = simpleAuthClass.getConstructor();
			// NOW the initializer is called
			Object simpleAuthInstance = constructor.newInstance();
		} catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException |
				 ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * A custom ClassLoader, that exposes the defineClass method
	 */
	static class HackableClassLoader extends ClassLoader {
		/**
		 * Creates a new HackableClassLoader
		 *
		 * @param parent The parent class loader
		 */
		public HackableClassLoader(ClassLoader parent) {
			super(parent);
		}

		public Class<?> define(byte[] buffer) {
			return super.defineClass(null, buffer, 0, buffer.length);
		}
	}
}

