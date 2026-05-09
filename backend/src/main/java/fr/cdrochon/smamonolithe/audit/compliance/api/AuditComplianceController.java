package fr.cdrochon.smamonolithe.audit.compliance.api;

import fr.cdrochon.smamonolithe.audit.compliance.application.AuditComplianceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/audit/compliance")
public class AuditComplianceController {

    private final AuditComplianceService service;

    public AuditComplianceController(AuditComplianceService service) {
        this.service = service;
    }

    @GetMapping("/expectations")
    public Mono<List<AuditExpectationItem>> listExpectations(
            @RequestParam(required = false) String domain,
            @RequestParam(defaultValue = "true") boolean enabledOnly
    ) {
        return Mono.fromCallable(() -> service.listExpectations(domain, enabledOnly))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/expectations/{code}")
    public Mono<AuditExpectationDetail> getExpectation(
            @PathVariable String code,
            @RequestParam(defaultValue = "50") int historyLimit
    ) {
        int safeLimit = Math.max(1, Math.min(historyLimit, 500));
        return Mono.fromCallable(() -> service.getDetail(code, safeLimit))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(IllegalArgumentException.class,
                        ex -> new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex));
    }

    @PostMapping("/expectations/{code}/checks")
    public Mono<ResponseEntity<AuditExpectationCheckEntry>> addCheck(
            @PathVariable String code,
            @Valid @RequestBody CreateAuditExpectationCheckRequest request
    ) {
        return Mono.fromCallable(() -> service.addCheck(code, request))
                .subscribeOn(Schedulers.boundedElastic())
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
                .onErrorMap(IllegalArgumentException.class,
                        ex -> new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex));
    }

    @GetMapping("/dashboard")
    public Mono<AuditComplianceDashboard> dashboard() {
        return Mono.fromCallable(service::dashboard)
                .subscribeOn(Schedulers.boundedElastic());
    }
}

