package fr.cdrochon.smamonolithe.event.query.entities;

import fr.cdrochon.smamonolithe.event.command.aggregate.GarageQueryAggregate;
import org.axonframework.modelling.command.AbstractRepository;
import org.axonframework.modelling.command.GenericJpaRepository;
import org.axonframework.springboot.autoconfig.JpaEventStoreAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GarageQueryRepository extends JpaRepository<GarageQuery, String> {
}
