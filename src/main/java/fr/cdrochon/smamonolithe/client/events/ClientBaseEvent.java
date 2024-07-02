package fr.cdrochon.smamonolithe.client.events;

import lombok.Getter;

public class ClientBaseEvent<T> {
    @Getter
    private final T id;
    
    public ClientBaseEvent(T id) {
        this.id = id;
    }
}
