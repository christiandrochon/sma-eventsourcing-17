package fr.cdrochon.smamonolithe.dossier.command.commands;

import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
public class DossierBaseCommand<T> {
    @TargetAggregateIdentifier
    final T id;
    
    public DossierBaseCommand(T id) {
        this.id = id;
    }
}
