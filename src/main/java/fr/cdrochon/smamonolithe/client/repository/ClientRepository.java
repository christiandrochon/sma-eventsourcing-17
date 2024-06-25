package fr.cdrochon.smamonolithe.client.repository;

import fr.cdrochon.smamonolithe.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
}
