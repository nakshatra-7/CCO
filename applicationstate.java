package com.company.ngos.common.workflow;

import com.company.ngos.common.enums.ApplicationStatus;

import java.util.Map;
import java.util.Set;

public final class ApplicationWorkflow {

    private ApplicationWorkflow() {
        // Utility class: prevent object creation
    }

    private static final Map<ApplicationStatus, Set<ApplicationStatus>>
            ALLOWED_TRANSITIONS = Map.ofEntries(

            Map.entry(
                    ApplicationStatus.APPLICATION_IN_PROGRESS,
                    Set.of(
                            ApplicationStatus.DOCUMENT_VERIFICATION_PENDING,
                            ApplicationStatus.CANCELLED
                    )
            ),

            Map.entry(
                    ApplicationStatus.DOCUMENT_VERIFICATION_PENDING,
                    Set.of(
                            ApplicationStatus.DOCUMENT_VERIFIED,
                            ApplicationStatus.CANCELLED
                    )
            ),

            Map.entry(
                    ApplicationStatus.DOCUMENT_VERIFIED,
                    Set.of(
                            ApplicationStatus.BUREAU_CHECK_PENDING
                    )
            ),

            Map.entry(
                    ApplicationStatus.BUREAU_CHECK_PENDING,
                    Set.of(
                            ApplicationStatus.BUREAU_CHECK_COMPLETED
                    )
            ),

            Map.entry(
                    ApplicationStatus.BUREAU_CHECK_COMPLETED,
                    Set.of(
                            ApplicationStatus.OFFER_GENERATED,
                            ApplicationStatus.REJECTED
                    )
            ),

            Map.entry(
                    ApplicationStatus.OFFER_GENERATED,
                    Set.of(
                            ApplicationStatus.OFFER_CONFIRMED,
                            ApplicationStatus.CANCELLED
                    )
            ),

            Map.entry(
                    ApplicationStatus.OFFER_CONFIRMED,
                    Set.of(
                            ApplicationStatus.PROCESSING
                    )
            ),

            Map.entry(
                    ApplicationStatus.PROCESSING,
                    Set.of(
                            ApplicationStatus.DISPATCHED
                    )
            )
    );

    private static final Set<ApplicationStatus> TERMINAL_STATES = Set.of(
            ApplicationStatus.DISPATCHED,
            ApplicationStatus.REJECTED,
            ApplicationStatus.CANCELLED
    );

    public static boolean canTransition(
            ApplicationStatus currentStatus,
            ApplicationStatus targetStatus
    ) {
        return ALLOWED_TRANSITIONS
                .getOrDefault(currentStatus, Set.of())
                .contains(targetStatus);
    }

    public static boolean isTerminal(ApplicationStatus status) {
        return TERMINAL_STATES.contains(status);
    }

    public static Set<ApplicationStatus> getAllowedTransitions(
            ApplicationStatus currentStatus
    ) {
        return ALLOWED_TRANSITIONS.getOrDefault(
                currentStatus,
                Set.of()
        );
    }
}