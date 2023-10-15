package me.exotic.jsec.generic;

import me.exotic.jsec.annotations.Runnable;

/**
 * @author native
 * Showcases generic System->getx techniques.
 */
public class GenericSysGets {

	@Runnable
	public static void getProperty() {
        /*
        System->getProperty and System->getEnv methods can be hooked to return a favoured result.
        Prefer natives over this.
         */
		String os = System.getProperty("os.name");
		String username = System.getProperty("user.name");

		System.out.println("OS: " + os);
		System.out.println("Username: " + username);
	}
}
