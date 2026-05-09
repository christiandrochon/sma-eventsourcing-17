package fr.cdrochon.smamonolithe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync  // nécessaire pour AuditService.record() asynchrone (RGPD non-bloquant)
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SmaMonolitheApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmaMonolitheApplication.class, args);
    }

}
