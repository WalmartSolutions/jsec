package me.exotic.jsec.breakdowns;

/**
 * @author native
 * Breakdown of 3000IQPlay's "SimpleAuth".
 */
public class SimpleAuth {

    /**
     * Pulled from <a href="https://github.com/3000IQPlay/SimpleAuth/blob/main/src/main/java/dev/_3000IQPlay/simpleauth/protect/antivm/VMDetector.java"></a><br>
     * The reason why I don't trust any of this is because it can all be bypassed by just hooking. <br>
     * The best approach to client-side authentication in my opinion is using natives instead of java.
     */
    public static void vmChecking() {
        /**
         * {@link me.exotic.jsec.generic.GenericSysGets}
         */
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("nux") || os.contains("win") || os.contains("mac")) {
            // Hooking can be performed here as well.

            /*
            String cpuInfo = execute("cat /proc/cpuinfo");
            if (cpuInfo.contains("hypervisor")) {
                // No VM :)
            } else {
                // VM :(
            }
             */
        }

        /*
        There are of course more checks but the fact is that I made my point clear.
        Now of course hooking is not a good idea to prevent every single one of these checks because there are so many.
        But I believe you can use hooking to not prevent the checks one by one but to find a way to prevent all of them at once.
        Which is why like I said the best approach for me is to just use natives and skip any of this.
        Of course natives can be broken as well but that's not the point, It's to annoy the attacker as much as possible.
        To essentially make them quit. (I have explained this before)
         */
    }
}
