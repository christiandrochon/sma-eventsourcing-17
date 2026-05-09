package fr.cdrochon.smamonolithe.dossier.events;

import lombok.Getter;

@Getter
public class DossierBaseEvent<T> {
    private final T id;
    public DossierBaseEvent(T id) {
        this.id = id;
    }
}
