package fr.cdrochon.smamonolithe.event.commonapi.command;

import lombok.Getter;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * Classe abstraite immutable qui represente l'agregat
 *
 * Ce sont les actions basées sur les regles metier
 * @param <T>
 */
public abstract class BaseCommand<T> {

    @TargetAggregateIdentifier
    @Getter private T id;

    public BaseCommand(T id) {
        this.id = id;
    }
}
