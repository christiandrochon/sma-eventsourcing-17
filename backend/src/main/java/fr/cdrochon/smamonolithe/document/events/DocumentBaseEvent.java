package fr.cdrochon.smamonolithe.document.events;

import lombok.Getter;

/**
 * Les events sont exprimés dans le passé (pour le nommage).
 * @param <T> le type de l'identifiant de l'entité
 */
@Getter
public class DocumentBaseEvent<T> {
    private final T id;
    public DocumentBaseEvent(T id) {
        this.id = id;
    }
}
