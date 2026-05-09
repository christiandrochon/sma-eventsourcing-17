package fr.cdrochon.smamonolithe.vehicule.event;

import lombok.Getter;

@Getter
public class VehiculeBaseEvent<T> {
    private final T id;
    public VehiculeBaseEvent(T id) {
        this.id = id;
    }
}
