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
//@Getter
//@SuperBuilder
//@RequiredArgsConstructor
public class BaseEvent<T> {
    @Getter
    private T id;

    public BaseEvent(T id) {
        this.id = id;
    }
}
