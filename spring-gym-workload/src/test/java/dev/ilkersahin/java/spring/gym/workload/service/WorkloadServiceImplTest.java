package dev.ilkersahin.java.spring.gym.workload.service;

import dev.ilkersahin.java.spring.gym.workload.dto.ActionType;
import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadRequest;
import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadResponse;
import dev.ilkersahin.java.spring.gym.workload.exception.TrainerNotFoundException;
import dev.ilkersahin.java.spring.gym.workload.model.MonthlySummary;
import dev.ilkersahin.java.spring.gym.workload.model.TrainerWorkload;
import dev.ilkersahin.java.spring.gym.workload.model.YearSummary;
import dev.ilkersahin.java.spring.gym.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceImplTest {

    @Mock
    private TrainerWorkloadRepository workloadRepository;

    @InjectMocks
    private WorkloadServiceImpl workloadService;

    // ── ADD ──────────────────────────────────────────────

    @Test
    void add_newTrainer_createsDocumentWithYearAndMonth() {
        var request = request(ActionType.ADD, "john.doe", "John", "Doe",
                true, LocalDate.of(2025, 3, 15), 60);
        when(workloadRepository.findById("john.doe")).thenReturn(Optional.empty());

        workloadService.processWorkload(request);

        var captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(workloadRepository).save(captor.capture());

        TrainerWorkload saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("john.doe");
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getYears()).hasSize(1);

        YearSummary year = saved.getYears().getFirst();
        assertThat(year.getYear()).isEqualTo(2025);
        assertThat(year.getMonths()).hasSize(1);
        assertThat(year.getMonths().getFirst().getMonth()).isEqualTo(3);
        assertThat(year.getMonths().getFirst().getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    void add_existingTrainer_newYear_addsYearEntry() {
        TrainerWorkload existing = workload("john.doe", 2024, 6, 100);
        when(workloadRepository.findById("john.doe")).thenReturn(Optional.of(existing));

        var request = request(ActionType.ADD, "john.doe", "John", "Doe",
                true, LocalDate.of(2025, 1, 10), 30);

        workloadService.processWorkload(request);

        verify(workloadRepository).save(existing);
        assertThat(existing.getYears()).hasSize(2);

        YearSummary newYear = existing.getYears().stream()
                .filter(y -> y.getYear() == 2025).findFirst().orElseThrow();
        assertThat(newYear.getMonths()).hasSize(1);
        assertThat(newYear.getMonths().getFirst().getMonth()).isEqualTo(1);
        assertThat(newYear.getMonths().getFirst().getTrainingSummaryDuration()).isEqualTo(30);
    }

    @Test
    void add_existingYear_newMonth_addsMonthEntry() {
        TrainerWorkload existing = workload("john.doe", 2025, 1, 100);
        when(workloadRepository.findById("john.doe")).thenReturn(Optional.of(existing));

        var request = request(ActionType.ADD, "john.doe", "John", "Doe",
                true, LocalDate.of(2025, 3, 5), 45);

        workloadService.processWorkload(request);

        verify(workloadRepository).save(existing);
        YearSummary year2025 = existing.getYears().getFirst();
        assertThat(year2025.getMonths()).hasSize(2);

        MonthlySummary march = year2025.getMonths().stream()
                .filter(m -> m.getMonth() == 3).findFirst().orElseThrow();
        assertThat(march.getTrainingSummaryDuration()).isEqualTo(45);
    }

    @Test
    void add_existingYearAndMonth_accumulatesDuration() {
        TrainerWorkload existing = workload("john.doe", 2025, 3, 100);
        when(workloadRepository.findById("john.doe")).thenReturn(Optional.of(existing));

        var request = request(ActionType.ADD, "john.doe", "John", "Doe",
                true, LocalDate.of(2025, 3, 20), 50);

        workloadService.processWorkload(request);

        verify(workloadRepository).save(existing);
        MonthlySummary march = existing.getYears().getFirst().getMonths().getFirst();
        assertThat(march.getTrainingSummaryDuration()).isEqualTo(150);
    }

    @Test
    void add_updatesTrainerProfileFields() {
        TrainerWorkload existing = workload("john.doe", 2025, 1, 10);
        existing.setFirstName("OldFirst");
        existing.setLastName("OldLast");
        existing.setActive(false);
        when(workloadRepository.findById("john.doe")).thenReturn(Optional.of(existing));

        var request = request(ActionType.ADD, "john.doe", "NewFirst", "NewLast",
                true, LocalDate.of(2025, 1, 5), 10);

        workloadService.processWorkload(request);

        assertThat(existing.getFirstName()).isEqualTo("NewFirst");
        assertThat(existing.getLastName()).isEqualTo("NewLast");
        assertThat(existing.getActive()).isTrue();
    }

    // ── DELETE ───────────────────────────────────────────

    @Test
    void delete_trainerNotFound_noSave() {
        when(workloadRepository.findById("unknown")).thenReturn(Optional.empty());

        var request = request(ActionType.DELETE, "unknown", "X", "Y",
                true, LocalDate.of(2025, 3, 1), 30);

        workloadService.processWorkload(request);

        verify(workloadRepository, never()).save(any());
    }

    @Test
    void delete_yearNotFound_noSave() {
        TrainerWorkload existing = workload("john.doe", 2024, 6, 100);
        when(workloadRepository.findById("john.doe")).thenReturn(Optional.of(existing));

        var request = request(ActionType.DELETE, "john.doe", "John", "Doe",
                true, LocalDate.of(2025, 3, 1), 30);

        workloadService.processWorkload(request);

        verify(workloadRepository, never()).save(any());
    }

    @Test
    void delete_monthNotFound_noSave() {
        TrainerWorkload existing = workload("john.doe", 2025, 1, 100);
        when(workloadRepository.findById("john.doe")).thenReturn(Optional.of(existing));

        var request = request(ActionType.DELETE, "john.doe", "John", "Doe",
                true, LocalDate.of(2025, 3, 1), 30);

        workloadService.processWorkload(request);

        verify(workloadRepository, never()).save(any());
    }

    @Test
    void delete_existingMonth_subtractsDuration() {
        TrainerWorkload existing = workload("john.doe", 2025, 3, 100);
        when(workloadRepository.findById("john.doe")).thenReturn(Optional.of(existing));

        var request = request(ActionType.DELETE, "john.doe", "John", "Doe",
                true, LocalDate.of(2025, 3, 1), 40);

        workloadService.processWorkload(request);

        verify(workloadRepository).save(existing);
        assertThat(existing.getYears().getFirst().getMonths().getFirst()
                .getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    void delete_subtractBelowZero_clampsToZero() {
        TrainerWorkload existing = workload("john.doe", 2025, 3, 20);
        when(workloadRepository.findById("john.doe")).thenReturn(Optional.of(existing));

        var request = request(ActionType.DELETE, "john.doe", "John", "Doe",
                true, LocalDate.of(2025, 3, 1), 50);

        workloadService.processWorkload(request);

        verify(workloadRepository).save(existing);
        assertThat(existing.getYears().getFirst().getMonths().getFirst()
                .getTrainingSummaryDuration()).isZero();
    }

    // ── GET ─────────────────────────────────────────────

    @Test
    void getTrainerWorkload_found_returnsResponse() {
        TrainerWorkload existing = workload("john.doe", 2025, 3, 120);
        existing.setFirstName("John");
        existing.setLastName("Doe");
        existing.setActive(true);
        when(workloadRepository.findById("john.doe")).thenReturn(Optional.of(existing));

        WorkloadResponse response = workloadService.getTrainerWorkload("john.doe");

        assertThat(response.trainerUsername()).isEqualTo("john.doe");
        assertThat(response.trainerFirstName()).isEqualTo("John");
        assertThat(response.trainerLastName()).isEqualTo("Doe");
        assertThat(response.trainerStatus()).isTrue();
        assertThat(response.years()).hasSize(1);
        assertThat(response.years().getFirst().year()).isEqualTo(2025);
        assertThat(response.years().getFirst().months()).hasSize(1);
        assertThat(response.years().getFirst().months().getFirst().month()).isEqualTo(3);
        assertThat(response.years().getFirst().months().getFirst().trainingSummaryDuration()).isEqualTo(120);
    }

    @Test
    void getTrainerWorkload_notFound_throwsException() {
        when(workloadRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workloadService.getTrainerWorkload("unknown"))
                .isInstanceOf(TrainerNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    // ── helpers ─────────────────────────────────────────

    private WorkloadRequest request(ActionType action, String username, String firstName,
                                    String lastName, boolean active, LocalDate date, int duration) {
        return new WorkloadRequest(username, firstName, lastName, active, date, duration, action);
    }

    private TrainerWorkload workload(String username, int year, int month, long duration) {
        TrainerWorkload w = new TrainerWorkload(username, "John", "Doe", true);
        YearSummary ys = new YearSummary(year);
        ys.getMonths().add(new MonthlySummary(month, duration));
        w.getYears().add(ys);
        return w;
    }
}
