package fr.cdrochon.smamonolithe.garage.command.commands;

import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * Classe abstraite immutable qui permet de donner un type generique à l'id d'une command
 * @param <T>
 */
@Getter
public class GarageBaseCommand<T> {

    @TargetAggregateIdentifier
    private final T id;

    public GarageBaseCommand(T id) {
        this.id = id;
    }
}
