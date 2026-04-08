package dev.ilkersahin.java.spring.gym.workload.listener;

import dev.ilkersahin.java.spring.gym.workload.dto.ActionType;
import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadRequest;
import dev.ilkersahin.java.spring.gym.workload.service.WorkloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {
    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private WorkloadMessageListener listener;

    @Test
    void validRequest_callsService() {
        var request = validRequest();

        listener.onWorkloadMessage(request, "tx-123");

        verify(workloadService).processWorkload(request);
    }

    @Test
    void validRequest_nullTransactionId_stillProcesses() {
        var request = validRequest();

        listener.onWorkloadMessage(request, null);

        verify(workloadService).processWorkload(request);
    }

    @ParameterizedTest(name = "[{index}] should reject request with invalid ''{1}''")
    @MethodSource("invalidRequests")
    void onWorkloadMessage_invalidRequest_throwsIllegalArgument(WorkloadRequest request, String expectedField) {
        assertThatThrownBy(() -> listener.onWorkloadMessage(request, "tx-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedField);

        verifyNoInteractions(workloadService);
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                Arguments.of(req(null, "John", "Doe", true, LocalDate.of(2025, 3, 1), 60, ActionType.ADD), "trainerUsername"),
                Arguments.of(req("  ", "John", "Doe", true, LocalDate.of(2025, 3, 1), 60, ActionType.ADD), "trainerUsername"),
                Arguments.of(req("john.doe", "John", "Doe", true, LocalDate.of(2025, 3, 1), 60, null), "actionType"),
                Arguments.of(req("john.doe", "John", "Doe", true, null, 60, ActionType.ADD), "trainingDate"),
                Arguments.of(req("john.doe", "John", "Doe", true, LocalDate.of(2025, 3, 1), 0, ActionType.ADD), "trainingDuration"),
                Arguments.of(req("john.doe", "John", "Doe", true, LocalDate.of(2025, 3, 1), -10, ActionType.ADD), "trainingDuration")
        );
    }

    private static WorkloadRequest req(
            String username, String firstName, String lastName,
            Boolean active, LocalDate date, int duration, ActionType action
    ) {
        return new WorkloadRequest(username, firstName, lastName, active, date, duration, action);
    }

    private WorkloadRequest validRequest() {
        return new WorkloadRequest("john.doe", "John", "Doe", true,
                LocalDate.of(2025, 3, 15), 60, ActionType.ADD);
    }
}
