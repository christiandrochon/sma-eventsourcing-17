package fr.cdrochon.smamonolithe.garage.command.commands;

import lombok.Getter;
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
