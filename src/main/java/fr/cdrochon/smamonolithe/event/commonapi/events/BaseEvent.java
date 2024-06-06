package fr.cdrochon.smamonolithe.event.commonapi.events;

import lombok.Getter;

/**
 * Objet immutable
 *
 * Ces events sont toujours exprimés au passé
 * @param <T>
 */
public class BaseEvent<T> {
    @Getter
    private T id;

    public BaseEvent(T id) {
        this.id = id;
    }
}
