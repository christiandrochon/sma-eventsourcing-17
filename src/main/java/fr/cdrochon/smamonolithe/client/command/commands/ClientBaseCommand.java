package fr.cdrochon.smamonolithe.client.command.commands;

import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * Classe abstraite immutable qui permet de donner un type generique à l'id d'une command
 *
 * @param <T>
 */
public class ClientBaseCommand<T> {
    @TargetAggregateIdentifier
    @Getter
    T id;
    
    public ClientBaseCommand(T id) {
        this.id = id;
    }
}
