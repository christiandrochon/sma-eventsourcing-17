package fr.cdrochon.smamonolithe.event.configuration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.axonframework.common.jpa.EntityManagerProvider;

public class MyEntityManagerProvider implements EntityManagerProvider {

    private EntityManager entityManager;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @PersistenceContext(unitName = "eventStore")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
