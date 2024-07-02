package fr.cdrochon.smamonolithe.garage.events;

import lombok.Getter;

/**
 * Objet immutable
 *
 * Ces events sont toujours exprimés au passé
 * @param <T>
 */


public class GarageBaseEvent<T> {
    @Getter private final T id;

    public GarageBaseEvent(T id) {
        this.id = id;
    }
}
