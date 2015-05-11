package de.ploing.osgitest;

import com.google.common.base.Joiner;
import de.ploing.osgitest.greetingservice.GreetingService;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;


public class OSGITest {
    public static void main(String[] args) throws Exception {
        System.out.println("OSGi Test");

        Path tmpDir = Files.createTempDirectory("osgicache");

        Map<String, String> config = new HashMap<String, String>();
        config.put(Constants.FRAMEWORK_STORAGE, tmpDir.toString());
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        /* DANGER, WILL ROBINSON!
            This will _override_ the package in all modules, making the modules' classes in this package inaccessible!
            Thus service2 will fail to load!
         */
        config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "de.ploing.osgitest.greetingservice");

        Framework framework = getFramework(config);
        try {
            doTests(framework.getBundleContext());
        } finally {
            framework.stop();
            framework.waitForStop(0);
            System.out.println("Please clean up tmpdir " + tmpDir);
        }
    }

    private static void doTests(BundleContext context) throws Exception {
        String[] bundles = {
                "file:service1/build/libs/service1-1.0.0.jar",
                "file:service2/build/libs/service2-1.0.0.jar" };

        // Try to load (and start) all bundles
        for (String bundleName: bundles) {
            try {
                Bundle bundle = context.installBundle(bundleName);
                if (bundle != null) {
                    bundle.start();
                } else {
                    System.err.println("Unable to install bundle " + bundleName);
                }
            } catch (BundleException e) {
                System.err.println("Unable to load bundle " + bundleName);
                e.printStackTrace();
            }
        }

        // Show list of all bundles, including their services
        System.out.println();
        System.out.println("List of bundles:");
        for (Bundle b: context.getBundles()) {
            System.out.println("  " + b.getSymbolicName() + " " + b.getVersion() + ": " + stateToString(b.getState()));
            if (b.getRegisteredServices()!=null) {
                System.out.println("   Services:");
                for (ServiceReference s : b.getRegisteredServices()) {
                    System.out.println("    " + Joiner.on(',').join((String[])s.getProperty("objectClass")));
                }
            }
        }

        // Get all available greeting services, make a test call
        System.out.println();
        System.out.println("Calling services:");
        Collection<ServiceReference<GreetingService>> refs = context.getServiceReferences(GreetingService.class, null);
        for (ServiceReference<GreetingService> ref: refs) {
            GreetingService g = context.getService(ref);
            System.out.println("  " + g.getGreeting());
        }
    }

    private static String stateToString(int state) {
        switch (state) {
            case Bundle.UNINSTALLED: return "uninstalled";
            case Bundle.INSTALLED: return "installed";
            case Bundle.RESOLVED: return "resolved";
            case Bundle.STARTING: return "starting";
            case Bundle.STOPPING: return "stopping";
            case Bundle.ACTIVE: return "active";
            default: return "unknown";
        }
    }

    private static Framework getFramework(Map<String, String> config) {
        FrameworkFactory factory = getFrameworkFactory();
        Framework framework = factory.newFramework(config);
        try {
            framework.init();
            framework.start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            return framework;
        } catch (BundleException e) {
            return null;
        }
    }

    private static FrameworkFactory getFrameworkFactory() {
        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class);
        for (FrameworkFactory f: loader) {
            if (f!=null) return f;
        }
        return null;
    }
}
