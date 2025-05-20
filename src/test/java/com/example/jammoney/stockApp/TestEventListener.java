package com.example.jammoney.stockApp;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestEventListener {
    private final List<Object> receivedEvents = new ArrayList<>();

    @EventListener
    public void handleAny(Object event) {
        receivedEvents.add(event);
    }

    public List<Object> getReceivedEvents() {
        return receivedEvents;
    }

    public void clear() {
        receivedEvents.clear();
    }
}
