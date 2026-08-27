## Purpose

This repository is a shared 7\-person Spring Boot \+ React credit\-card origination system\. Follow this file and `WORKFLOW.md` strictly\. Do not invent parallel conventions, duplicate shared infrastructure, or change shared contracts from a feature branch without team approval\.

## 1\. Non\-negotiable guardrails

- DO NOT modify shared/common infrastructure unless the task explicitly requires it and the team agrees\.
- DO NOT create duplicate enums, error models, exception hierarchies, JWT/security utilities, workflow logic, logging utilities, API clients, or generic abstractions\.
- DO NOT bypass `ApplicationWorkflow` or directly mutate application status from arbitrary feature code\.
- DO NOT expose JPA entities directly from controllers\. Use request/response DTOs\.
- DO NOT parse/validate JWTs inside feature modules\.
- DO NOT return ad\-hoc error JSON from controllers\.
- DO NOT use `System.out.println`\.
- DO NOT log passwords, JWTs, Authorization headers, full request bodies, document contents, or sensitive card/identity data\.
- DO NOT hardcode secrets, credentials, external URLs, or environment\-specific values\.
- DO NOT add new business fields, roles, statuses, enums, endpoints, or workflow states merely because they are “common in banking\.” They must come from agreed requirements\.

## 2\. Shared files are protected

Treat these as canonical and effectively read\-only from feature branches unless the team explicitly approves a change:

- `common/enums/**`
- `common/workflow/ApplicationWorkflow.java`
- `WORKFLOW.md`
- `common/error/**`
- `common/exception/**`
- `common/logging/**`
- `security/**`
- shared frontend `api/apiClient.js`
- shared configuration and cross\-cutting conventions in this file

If a shared contract appears insufficient, raise it for discussion instead of creating a local replacement\.

## 3\. Canonical enums

Use the existing shared enums only:

- `UserRole` &#40;`SALES_USER`, `OPS_USER`&#41;
- `Gender`
- `CustomerType`
- `AccountType`
- `CardType`
- `ApplicationStatus`
- `DocumentStatus`
- `OfferStatus`

Import and reuse them\. Never replace them with strings or create feature\-local duplicates\.

`DocumentType` is NOT an enum\. Document types come from the `DOCUMENT_TYPE` database table\.

## 4\. Application workflow

`ApplicationStatus` defines legal states\. `ApplicationWorkflow` defines legal transitions\. `WORKFLOW.md` is the human\-readable source of truth\.

Rules:

- All status changes must be validated through `ApplicationWorkflow`\.
- Frontend clients must never set arbitrary application status\.
- Feature services may request a legal transition; they must not skip intermediate states\.
- `DISPATCHED`, `REJECTED`, and `CANCELLED` are terminal states\.
- Retryable technical failures such as document\-verification failure and bureau\-check failure must follow the transitions defined in `ApplicationWorkflow`\.
- Never add/rename/remove workflow states from a feature branch without team approval\.

## 5\. Backend package structure

Use feature\-based modules\. A feature may contain only what it needs:

```text
feature/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── mapper/
└── client/        # only for external-system integrations
```

Do not create global `controller/`, `service/`, or `repository/` buckets\. Do not add `Utils.java`, `GenericService`, `GenericRepository`, `GenericMapper`, or similar abstractions unless there is a proven repeated need\.

Expected call direction:

```text
Controller -> Service -> Repository / External Client
```

Controllers must not call repositories directly\.

## 6\. API and DTO conventions

- Base path: `/api/v1`
- REST paths: lowercase plural nouns\.
- JSON: camelCase\.
- Java classes: PascalCase\.
- Java fields/methods: camelCase\.
- Java constants / enum values: UPPER\_SNAKE\_CASE\.
- PostgreSQL tables/columns: snake\_case\.
- Use request/response DTOs, e\.g\. `CreateApplicationRequest`, `UpdateApplicationRequest`, `ApplicationResponse`\.
- Never trust client\-supplied ownership/security fields such as `salesUserId`, role, or arbitrary status\. Derive them from authenticated backend context\.
- Each developer owns and documents the API contract for their feature, then gets it reviewed before broad integration\.
- API contract must define: method, path, allowed role&#40;s&#41;, request, response, success status, errors, preconditions, and relevant workflow transition\.

## 7\. Validation

Use Jakarta Bean Validation on request DTOs:

- `@NotNull` for required non\-string values
- `@NotBlank` for required strings
- `@Email`
- `@Positive`
- `@Size`
- `@Pattern`
- `@Valid` for nested DTOs

Use `@Valid` at controller boundaries\. Do not hand\-code ordinary field validation in controllers\.

## 8\. Errors and exceptions

All API failures must use the existing shared error layer:

- `ApiError`
- `FieldErrorDetail`
- `ErrorCode`
- `GlobalExceptionHandler`
- shared exceptions such as `ResourceNotFoundException`, `BusinessRuleException`, `ConflictException`, `ExternalServiceException`, `ExternalServiceTimeoutException`

Feature code should throw the appropriate shared exception\. Controllers must not manually construct error responses\.

Use existing broad `ErrorCode` values\. Do not create extremely granular codes when `VALIDATION_ERROR`, `INVALID_REQUEST`, `RESOURCE_NOT_FOUND`, etc\. plus a specific message/field error already express the problem\.

## 9\. Logging and correlation IDs

Use SLF4J \+ Logback only\.

- INFO: important successful business events
- WARN: expected/recoverable abnormal conditions
- ERROR: failed operations/unexpected exceptions
- DEBUG: development diagnostics

Use `{}` placeholders, not string concatenation\.
Correlation IDs are generated globally and stored in MDC\. Feature code must not generate its own correlation IDs\.

## 10\. Authentication and authorization

Spring Security \+ JWT is global infrastructure\.

Feature modules MUST NOT:

- parse JWTs
- validate JWT signatures/expiry
- read/interpret Authorization headers manually
- create their own JWT filters/services
- implement separate 401/403 response formats

Global security authenticates protected `/api/v1/**` requests before feature controllers execute\.

Feature developers ARE responsible for authorization intent:

- decide whether an endpoint is available to `SALES_USER`, `OPS_USER`, or both
- enforce role restrictions with Spring Security mechanisms such as `@PreAuthorize`

Examples:

```java
@PreAuthorize("hasRole('OPS_USER')")
```

```java
@PreAuthorize("hasAnyRole('SALES_USER', 'OPS_USER')")
```

Do not rely on hidden frontend buttons for security\. Backend authorization is mandatory for privileged actions\.

The authenticated user identity must come from Spring Security context/principal, not from request\-body fields\.

Person 1 owns login, user lookup/password verification, JWT generation and JWT parsing implementation\. Shared security owns request\-time authentication enforcement, security context, 401 handling, 403 handling, and role authorization infrastructure\.

## 11\. Frontend\-backend integration

All frontend API calls must go through the shared `api/apiClient.js`\.

`apiClient.js` owns common HTTP concerns:

- backend base URL
- Authorization header attachment
- common timeout/config
- common 401 handling
- common response/error plumbing

Feature\-specific endpoint functions belong in feature API files such as:

- `authApi.js`
- `applicationApi.js`
- `customerApi.js`
- `documentApi.js`
- `bureauApi.js`
- `offerApi.js`
- `fulfilmentApi.js`
- `reportingApi.js`

Do not call hardcoded backend URLs directly from React components/pages\.

## 12\. External integrations

Bureau, CCMS, Transaction Processing, Mail, PrintShop, etc\. must be accessed through dedicated client classes\. Do not scatter raw HTTP calls through controllers/services\.

Map external failures to the shared exception layer\. Do not expose external/internal exception details directly to the frontend\.

## 13\. Database / entity rules

- Use Spring Data JPA\.
- Persist enums with `EnumType.STRING`, never ordinal values\.
- Respect the agreed ERD and ownership of entities\.
- Do not add duplicate columns merely for frontend convenience\.
- Do not introduce a new entity/table from a feature branch without discussion\.
- Database migration tooling is intentionally deferred unless the team explicitly starts that phase\.

## 14\. Feature\-branch discipline

Before coding:

1. Pull/rebase latest shared foundation\.
2. Read `AGENTS.md` and `WORKFLOW.md`\.
3. Confirm the feature’s API contract and role permissions\.
4. Reuse existing shared types before creating anything new\.

While coding:

- Stay inside your feature package except for approved shared changes\.
- Keep business logic in services\.
- Add tests for happy path, validation failure, business\-rule failure, and authorization\-sensitive behavior where applicable\.

Before PR:

- Project compiles\.
- No duplicated shared infrastructure\.
- No hardcoded secrets/URLs\.
- No direct entity exposure\.
- No manual JWT parsing\.
- No direct application\-status bypass\.
- Errors follow `ApiError`\.
- Logging follows the common rules\.
- API contract matches implementation\.
- Frontend feature API uses `apiClient.js`\.

## 15\. Conflict rule

If this file, `WORKFLOW.md`, shared enums, security rules, or agreed API contracts conflict with generated/copilot suggestions, FOLLOW THE REPOSITORY RULES\. Do not silently “improve” architecture or requirements\. Raise the conflict for team review\.
