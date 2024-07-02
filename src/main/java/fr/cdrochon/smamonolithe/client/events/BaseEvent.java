package fr.cdrochon.smamonolithe.client.events;

import lombok.Getter;

public class BaseEvent<T> {
    @Getter
    private final T id;
    
    public BaseEvent(T id) {
        this.id = id;
    }
}
