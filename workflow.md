# Credit Card Application Workflow

## Application States

APPLICATION_IN_PROGRESS
        |
        v
DOCUMENT_VERIFICATION_PENDING
        |
        v
DOCUMENT_VERIFIED
        |
        v
BUREAU_CHECK_PENDING
        |
        v
BUREAU_CHECK_COMPLETED
       / \
      /   \
REJECTED  OFFER_GENERATED
              |
              v
        OFFER_CONFIRMED
              |
              v
          PROCESSING
              |
              v
          DISPATCHED


Workflow Rules:

1. Every new credit card application starts with
   APPLICATION_IN_PROGRESS.

2. After the application is submitted for document verification,
   it moves to DOCUMENT_VERIFICATION_PENDING.

3. The application can move to DOCUMENT_VERIFIED only when all
   mandatory documents are verified.

4. Bureau check can start only after DOCUMENT_VERIFIED.

5. When the Bureau request starts, the application moves to
   BUREAU_CHECK_PENDING.

6. After a successful Bureau response and credit score retrieval,
   the application moves to BUREAU_CHECK_COMPLETED.

7. After the Bureau check:
   - Eligible application -> OFFER_GENERATED
   - Ineligible application -> REJECTED

8. An offer must be generated before it can be confirmed.

9. After offer confirmation, downstream card processing starts and
   the application moves to PROCESSING.

10. After successful card processing and dispatch initiation,
    the application moves to DISPATCHED.

11. DISPATCHED, REJECTED and CANCELLED are terminal states.

12. Frontend requests must never directly set ApplicationStatus.

13. All application status changes must be validated using
    ApplicationWorkflow.

14. Application cancellation is currently allowed only from:
    APPLICATION_IN_PROGRESS,
    DOCUMENT_VERIFICATION_PENDING,
    OFFER_GENERATED.