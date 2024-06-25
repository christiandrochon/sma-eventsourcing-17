package fr.cdrochon.smamonolithe.event.commonapi.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * Classe abstraite immutable qui permet de donner un type generique à l'id d'une command
 * @param <T>
 */
//@Getter
//@SuperBuilder
//@RequiredArgsConstructor
public class BaseCommand<T> {

    @TargetAggregateIdentifier
    @Getter
    private T id;

    public BaseCommand(T id) {
        this.id = id;
    }
}
