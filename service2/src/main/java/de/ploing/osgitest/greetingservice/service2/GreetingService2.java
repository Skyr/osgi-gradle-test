package de.ploing.osgitest.greetingservice.service2;

import de.ploing.osgitest.greetingservice.GreetingService;


public class GreetingService2 implements GreetingService {
    @Override
    public String getGreeting() {
        return "Greeting from service 2";
    }
}
