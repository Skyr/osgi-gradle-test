package de.ploing.osgitest.greetingservice;

import de.ploing.osgitest.greetingservice.service2.GreetingService2;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class Service2Activator implements BundleActivator {
    private ServiceRegistration registration;

    @Override
    public void start(BundleContext context) throws Exception {
        registration = context.registerService(GreetingService.class.getName(), new GreetingService2(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }
}
