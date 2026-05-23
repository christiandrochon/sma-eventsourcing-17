# Security Review Checklist

Checklist periodique (mensuelle/trim.) pour eviter les regressions de securite.

## 1) Endpoints publics

- [ ] Reviser les `pathMatchers(...).permitAll()` dans `backend/.../SecurityConfig.java`.
- [ ] Confirmer que seuls Swagger/Actuator strictement necessaires sont ouverts.

## 2) Swagger

- [ ] `prod`: `springdoc.swagger-ui.enabled=false` et `springdoc.api-docs.enabled=false` (ou acces strictement controle).
- [ ] `dev`: acces doc conforme au besoin de l'equipe.

## 3) CORS

- [ ] Verifier `corsConfigurationSource` et les origines autorisees.
- [ ] Eviter `*` en production publique sans contrainte complementaire.

## 4) RBAC

- [ ] Verifier mapping roles `ADMIN/USER/AUDITOR`.
- [ ] Rejouer tests RBAC/IDOR et smoke-check manuel.

## 5) Audit

- [ ] Verifier acces `/audit/compliance/**` (GET roles audit/admin, POST admin).
- [ ] Verifier traces dans `audit_events` et logs securite.

