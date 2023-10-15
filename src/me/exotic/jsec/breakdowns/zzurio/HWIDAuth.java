package me.exotic.jsec.breakdowns.zzurio;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author native
 * Breakdown of zzurio's Hardware based authentication system.
 * WARNING: If obfuscated this may not be the same.
 */
public class HWIDAuth {
    /*
    The way zzurio's authentication system works is by combining the computer's information.
    The information is then hashed with SHA-256 and compared to already existing hashes on a server.
    If the local hash and the server hash match, you pass if they don't you fail.
    However, there are several problems with it. Let's look at a few of them.
     */

    /**
     * The first flaw we see is the usage of {@link java.io.BufferedReader}.
     * This problem is the fact that it can be hooked as many of the techniques employed here.
     * Meaning we can obtain a lot of information from this, for example:
     * We can obtain the URL of the server.
     * We can obtain the content of the reader.
     * All of this can be done by just simply hooking.
     */
    public void auth() {
        try {
            /**
             * Pulled from <a href="https://github.com/zzurio/HWID-Authentication-System/blob/main/src/main/java/club/cpacket/hwid/util/URLReader.java"></>
             * A {@link java.net.URL} object is created.
             * Passing the url as a string into the constructor of {@link java.net.URL}.
             * The main problem with this is the fact that {@link java.net.URL} can be hooked to log what is passed.
             * This means we can obtain the url that is passed in.
             */
            URL url = new URL("https://github.com/WalmartSolutions");
        } catch (MalformedURLException ignored) {}

        /**
         * Pulled from <a href="https://github.com/zzurio/HWID-Authentication-System/blob/main/src/main/java/club/cpacket/hwid/manager/HWIDManager.java"></a>
         * The next flaw includes getting the computer's information and comparing it.
         * Problem with it is that this is way too simple.
         * This can be simply cracked by just inverting the if statement in an assembler.
         * I will not explain how to do that because that is just going too far.
         * I won't hold your hand through everything.
         */

        /*
         boolean isHwidPresent = hwids.contains(SystemUtil.getSystemInfo());
         if (!isHwidPresent) {
            DisplayUtil.Display();
            throw new NoStackTraceThrowable("");
         }
         */

        /**
         * Pulled from <a href="https://github.com/zzurio/HWID-Authentication-System/blob/main/src/main/java/club/cpacket/hwid/HWIDAuthMod.java"></a>
         * The next flaw is the way you are supposed to call it/use it.
         * I suggest coming up with a more clever way of doing this instead of just blatantly calling it in the Main class.
         * This will be visible in bytecode and the reference to hwidCheck() can be removed and the software cracked.
         */

        /*
        @EventHandler
        public void preInit(FMLPreInitializationEvent event) {
            HWIDManager.hwidCheck();
        }
         */

        /**
         * This was very simple because there really isn't much to say about this authentication system.
         * It just simply isn't that good (no offense zzurio) but it isn't meant to be.
         * I showed only a few flaws with it but there are more.
         * I won't show any others you will need to find them yourself.
         * A way on improving this is transpiling everything into natives using a native obfuscator.
         * I recommend using <a href="https://jnic.dev/"></a> for this task.
         * But even then you won't be safe.
         */
    }
}
