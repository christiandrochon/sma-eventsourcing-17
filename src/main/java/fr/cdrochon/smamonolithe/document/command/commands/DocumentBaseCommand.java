package fr.cdrochon.smamonolithe.document.command.commands;

import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
@Getter
public class DocumentBaseCommand<T> {
    @TargetAggregateIdentifier
    T id;
    public DocumentBaseCommand(T id) {
        this.id = id;
    }
}
