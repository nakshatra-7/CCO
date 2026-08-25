# Credit Card Application Workflow

## Application Flow

```text
APPLICATION_CREATED
        |
        v
DOCUMENT_VERIFICATION_PENDING
        |
        |---- Verification Failed ----> DOCUMENT_VERIFICATION_FAILED
        |                                      |
        |                                      | Retry
        |                                      v
        |                           DOCUMENT_VERIFICATION_PENDING
        |
        |---- Documents Invalid ----> REJECTED
        |
        v
DOCUMENT_VERIFIED
        |
        v
BUREAU_CHECK_PENDING
        |
        |---- Bureau/API Failed ----> BUREAU_CHECK_FAILED
        |                                  |
        |                                  | Retry
        |                                  v
        |                           BUREAU_CHECK_PENDING
        |
        v
BUREAU_CHECK_COMPLETED
        |
        |---- Ineligible -----------> REJECTED
        |
        v
OFFER_GENERATED
        |
        v
OFFER_CONFIRMED
        |
        v
PROCESSING
        |
        v
DISPATCHED
```

## Cancellation

```text
APPLICATION_CREATED
        |
        v
CANCELLED
```

## Terminal States

```text
DISPATCHED
REJECTED
CANCELLED
```

## Workflow Rules

* Every new application starts with `APPLICATION_CREATED`.
* Documents must be verified before a Bureau check can begin.
* `DOCUMENT_VERIFICATION_FAILED` represents a technical verification failure and can be retried.
* Invalid documents result in `REJECTED`.
* `BUREAU_CHECK_FAILED` represents an API or technical failure and can be retried.
* A successful Bureau check can result in either `OFFER_GENERATED` or `REJECTED`.
* An offer must be generated before it can be confirmed.
* Processing begins only after offer confirmation.
* `DISPATCHED`, `REJECTED`, and `CANCELLED` are terminal states.
* The frontend must not directly change `ApplicationStatus`.
* All status changes must follow the transitions defined in `ApplicationWorkflow.java`.
