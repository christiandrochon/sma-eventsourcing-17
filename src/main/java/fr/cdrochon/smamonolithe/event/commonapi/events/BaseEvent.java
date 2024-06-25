package fr.cdrochon.smamonolithe.event.commonapi.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

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
