Purpose
This repository is a shared 7\-person Spring Boot \+ React credit\-card origination system\. Follow this file and WORKFLOW\.md strictly\. Do not invent parallel conventions, duplicate shared infrastructure, or change shared contracts from a feature branch without team approval\.

1. Non\-negotiable guardrails
   DO NOT modify shared/common infrastructure unless the task explicitly requires it and the team agrees\.
   DO NOT create duplicate enums, error models, exception hierarchies, JWT/security utilities, workflow logic, logging utilities, API clients, or generic abstractions\.
   DO NOT bypass ApplicationWorkflow or directly mutate application status from arbitrary feature code\.
   DO NOT expose JPA entities directly from controllers\. Use request/response DTOs\.
   DO NOT parse or validate JWTs inside feature modules\.
   DO NOT return ad\-hoc error JSON from controllers\.
   DO NOT use System\.out\.println\.
   DO NOT log passwords, JWTs, Authorization headers, full request bodies, document contents, or sensitive card/identity data\.
   DO NOT hardcode secrets, credentials, external URLs, or environment\-specific values\.
   DO NOT add new business fields, roles, statuses, enums, endpoints, or workflow states unless they come from agreed requirements\.
2. Shared files are protected
   Treat these as canonical and read\-only from feature branches unless the team explicitly approves a change:
   common/enums/
   common/workflow/ApplicationWorkflow\.java
   WORKFLOW\.md
   common/error/
   common/exception/
   common/logging/
   security/
   frontend shared api/apiClient\.js
   shared configuration and conventions in this file

If a shared contract appears insufficient, raise it for discussion instead of creating a local replacement\.

3. Canonical enums
   Use only the existing shared enums:
   UserRole: SALES\_USER, OPS\_USER
   Gender
   CustomerType
   AccountType
   CardType
   ApplicationStatus
   DocumentStatus
   OfferStatus

Import and reuse them\. Never replace them with strings or create feature\-local duplicates\.

DocumentType is NOT an enum\. Document types come from the DOCUMENT\_TYPE database table\.

4. Application workflow
   ApplicationStatus defines legal states\.
   ApplicationWorkflow defines legal transitions\.
   WORKFLOW\.md is the human\-readable source of truth\.

All status changes must be validated through ApplicationWorkflow\.
Frontend clients must never set arbitrary application status\.
Feature services must not skip intermediate workflow states\.
DISPATCHED, REJECTED, and CANCELLED are terminal states\.
Retryable technical failures must follow ApplicationWorkflow\.
Never add, rename, or remove workflow states from a feature branch without team approval\.

5. Backend package structure
   Use feature\-based modules\.

Typical feature structure:
feature/
controller/
service/
repository/
entity/
dto/
mapper/
client/ only when the feature integrates with an external system

Expected call direction:
Controller \-\> Service \-\> Repository or External Client

Controllers must not call repositories directly\.
Do not create global controller/service/repository buckets\.
Do not add Utils\.java, GenericService, GenericRepository, GenericMapper, or similar abstractions unless there is a proven repeated need\.

6. API and DTO conventions
   Base path: /api/v1
   REST paths: lowercase plural nouns
   JSON: camelCase
   Java classes: PascalCase
   Java fields and methods: camelCase
   Java constants and enum values: UPPER\_SNAKE\_CASE
   PostgreSQL tables and columns: snake\_case

Use request/response DTOs such as:
CreateApplicationRequest
UpdateApplicationRequest
ApplicationResponse

Never trust client\-supplied security or ownership fields such as salesUserId, role, or arbitrary application status\. Derive them from authenticated backend context\.

Each developer owns and documents the API contract for their feature and gets it reviewed before broad integration\.

Each API contract must define:
HTTP method
path
allowed role or roles
request
response
success status
errors
preconditions
relevant workflow transition

7. Validation
   Use Jakarta Bean Validation on request DTOs\.

Use:
@NotNull for required non\-string fields
@NotBlank for required strings
@Email for email
@Positive for positive numbers
@Size for controlled lengths
@Pattern for formatted values
@Valid for nested DTOs

Use @Valid at controller boundaries\.
Do not hand\-code ordinary field validation in controllers\.

8. Errors and exceptions
   All API failures must use the existing shared error layer:
   ApiError
   FieldErrorDetail
   ErrorCode
   GlobalExceptionHandler
   ResourceNotFoundException
   BusinessRuleException
   ConflictException
   ExternalServiceException
   ExternalServiceTimeoutException

Feature code should throw the appropriate shared exception\.
Controllers must not manually construct error responses\.
Use existing broad ErrorCode values instead of creating very granular duplicates\.

9. Logging and correlation IDs
   Use SLF4J \+ Logback only\.

INFO: important successful business events
WARN: expected or recoverable abnormal conditions
ERROR: failed operations or unexpected exceptions
DEBUG: development diagnostics

Use placeholder logging, not string concatenation\.
Correlation IDs are generated globally and stored in MDC\.
Feature code must not generate its own correlation IDs\.

Never log passwords, JWTs, Authorization headers, document contents, or sensitive identity/card data\.

10. Authentication and authorization
    Spring Security \+ JWT is global infrastructure\.

Feature modules MUST NOT:
parse JWTs
validate JWT signatures or expiry
read or interpret Authorization headers manually
create their own JWT filters or JWT services
implement separate 401 or 403 response formats

Global security authenticates protected /api/v1/\*\* requests before feature controllers execute\.

Feature developers ARE responsible only for authorization intent:
decide whether an endpoint is available to SALES\_USER, OPS\_USER, or both
enforce that using Spring Security authorization such as @PreAuthorize

Example:
@PreAuthorize&#40;“hasRole&#40;‘OPS\_USER’&#41;”&#41;

Example:
@PreAuthorize&#40;“hasAnyRole&#40;‘SALES\_USER’, ‘OPS\_USER’&#41;”&#41;

Do not rely on hidden frontend buttons for security\.
Backend authorization is mandatory for privileged actions\.

The authenticated user identity must come from Spring Security context/principal, not from request\-body fields\.

Person 1 owns:
login
user lookup
password verification
JWT generation
JWT claim structure
JWT parsing implementation

Shared security owns:
request\-time authentication enforcement
SecurityContext setup
401 handling
403 handling
role\-based authorization infrastructure

11. Frontend\-backend integration
    All frontend API calls must go through the shared api/apiClient\.js\.

apiClient\.js owns:
backend base URL
Authorization header attachment
common timeout/config
common 401 handling
common response/error plumbing

Feature\-specific endpoint functions belong in:
authApi\.js
applicationApi\.js
customerApi\.js
documentApi\.js
bureauApi\.js
offerApi\.js
fulfilmentApi\.js
reportingApi\.js

Do not call hardcoded backend URLs directly from React components or pages\.

12. External integrations
    Bureau, CCMS, Transaction Processing, Mail, PrintShop, and other external systems must be accessed through dedicated client classes\.

Do not scatter raw HTTP calls through controllers or services\.
Map external failures to the shared exception layer\.
Do not expose external/internal exception details directly to the frontend\.

13. Database and entity rules
    Use Spring Data JPA\.
    Persist enums with EnumType\.STRING, never ordinal values\.
    Respect the agreed ERD and entity ownership\.
    Do not add duplicate columns for frontend convenience\.
    Do not introduce a new entity or table from a feature branch without discussion\.
    Database migration tooling is intentionally deferred unless the team explicitly starts that phase\.
14. Feature\-branch discipline
    Before coding:
    Pull or rebase the latest shared foundation\.
    Read AGENTS\.md and WORKFLOW\.md\.
    Confirm the feature API contract and role permissions\.
    Reuse existing shared types before creating anything new\.

While coding:
Stay inside your feature package except for approved shared changes\.
Keep business logic in services\.
Add tests for happy path, validation failure, business\-rule failure, and authorization\-sensitive behavior where applicable\.

Before PR:
Project compiles\.
No duplicated shared infrastructure\.
No hardcoded secrets or URLs\.
No direct entity exposure\.
No manual JWT parsing\.
No direct application\-status bypass\.
Errors follow ApiError\.
Logging follows common rules\.
API contract matches implementation\.
Frontend feature API uses apiClient\.js\.

15. Conflict rule
    If AGENTS\.md, WORKFLOW\.md, shared enums, security rules, or agreed API contracts conflict with generated/copilot suggestions, FOLLOW THE REPOSITORY RULES\.

Do not silently redesign architecture or requirements\.
Raise the conflict for team review\.
