package fr.cdrochon.smamonolithe.audit.compliance.application;

import fr.cdrochon.smamonolithe.audit.compliance.api.*;
import fr.cdrochon.smamonolithe.audit.compliance.infrastructure.AuditComplianceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditComplianceService {

    private final AuditComplianceRepository repository;

    public AuditComplianceService(AuditComplianceRepository repository) {
        this.repository = repository;
    }

    public List<AuditExpectationItem> listExpectations(String domain, boolean enabledOnly) {
        return repository.listExpectations(domain, enabledOnly);
    }

    public AuditExpectationDetail getDetail(String code, int historyLimit) {
        AuditExpectationItem expectation = repository.findExpectation(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown expectation code: " + code));
        List<AuditExpectationCheckEntry> history = repository.listChecks(code, historyLimit);
        return new AuditExpectationDetail(expectation, history);
    }

    public AuditExpectationCheckEntry addCheck(String code, CreateAuditExpectationCheckRequest request) {
        repository.findExpectation(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown expectation code: " + code));
        return repository.insertCheck(code, request);
    }

    public AuditComplianceDashboard dashboard() {
        return repository.dashboard();
    }
}

