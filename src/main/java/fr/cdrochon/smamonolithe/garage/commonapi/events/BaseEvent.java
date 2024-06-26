package fr.cdrochon.smamonolithe.garage.commonapi.events;

import lombok.Getter;

/**
 * Objet immutable
 *
 * Ces events sont toujours exprimés au passé
 * @param <T>
 */


public class BaseEvent<T> {
    @Getter private final T id;

    public BaseEvent(T id) {
        this.id = id;
    }
}
