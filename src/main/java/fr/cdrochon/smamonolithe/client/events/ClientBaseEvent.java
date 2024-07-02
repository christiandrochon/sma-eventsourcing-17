package fr.cdrochon.smamonolithe.client.events;

import lombok.Getter;
@Getter
public class ClientBaseEvent<T> {
    
    private final T id;
    public ClientBaseEvent(T id) {
        this.id = id;
    }
}
