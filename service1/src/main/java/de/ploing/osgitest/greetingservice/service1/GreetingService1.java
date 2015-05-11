package de.ploing.osgitest.greetingservice.service1;

import de.ploing.osgitest.greetingservice.GreetingService;


public class GreetingService1 implements GreetingService {
    @Override
    public String getGreeting() {
        return "Greeting from service 1";
    }
}
