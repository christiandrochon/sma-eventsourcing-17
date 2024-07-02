package fr.cdrochon.smamonolithe.garage.events;

import lombok.Getter;

/**
 * Objet immutable
 * <p>
 * Ces events sont toujours exprimés au passé
 *
 * @param <T> le type de l'identifiant de l'entité
 */


public class GarageBaseEvent<T> {
    @Getter
    private final T id;
    
    public GarageBaseEvent(T id) {
        this.id = id;
    }
}
