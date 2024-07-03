package fr.cdrochon.smamonolithe.vehicule.command.commands;

import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
public class VehiculeBaseCommand<T> {
    
    @TargetAggregateIdentifier
    T id;
    
    public VehiculeBaseCommand(T id) {
        this.id = id;
    }
}
