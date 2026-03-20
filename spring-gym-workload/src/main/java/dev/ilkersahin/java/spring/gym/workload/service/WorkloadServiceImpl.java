package dev.ilkersahin.java.spring.gym.workload.service;

import dev.ilkersahin.java.spring.gym.workload.dto.ActionType;
import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadRequest;
import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadResponse;
import dev.ilkersahin.java.spring.gym.workload.model.MonthlySummary;
import dev.ilkersahin.java.spring.gym.workload.model.TrainerWorkload;
import dev.ilkersahin.java.spring.gym.workload.repository.TrainerWorkloadRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceImpl implements WorkloadService {
    private final TrainerWorkloadRepository workloadRepository;

    @Override
    public void processWorkload(WorkloadRequest request) {
        int year = request.trainingDate().getYear();
        int month = request.trainingDate().getMonthValue();

        if (request.actionType() == ActionType.ADD) {
            handleAdd(request, year, month);
        } else {
            handleDelete(request, year, month);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WorkloadResponse getTrainerWorkload(String username) {
        TrainerWorkload workload = workloadRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainer workload not found for username: " + username
                ));
        return toResponse(workload);
    }

    private void handleAdd(WorkloadRequest request, int year, int month) {
        TrainerWorkload workload = workloadRepository.findById(request.trainerUsername())
                .orElseGet(() -> new TrainerWorkload(
                        request.trainerUsername(),
                        request.trainerFirstName(),
                        request.trainerLastName(),
                        request.isActive()
                ));

        workload.setFirstName(request.trainerFirstName());
        workload.setLastName(request.trainerLastName());
        workload.setActive(request.isActive());

        MonthlySummary summary = findOrCreateMonthlySummary(workload, year, month);
        summary.setTrainingSummaryDuration(
                summary.getTrainingSummaryDuration() + request.trainingDuration()
        );

        workloadRepository.save(workload);

        log.info("ADD: trainer='{}', year={}, month={}, added={} min, total={} min",
                request.trainerUsername(), year, month,
                request.trainingDuration(), summary.getTrainingSummaryDuration());
    }

    private void handleDelete(WorkloadRequest request, int year, int month) {
        TrainerWorkload workload = workloadRepository.findById(request.trainerUsername())
                .orElse(null);

        if (workload == null) {
            log.warn("DELETE no-op: trainer '{}' not found in workload database",
                    request.trainerUsername());
            return;
        }

        workload.getMonthlySummaries().stream()
                .filter(s -> s.getYear() == year && s.getMonth() == month)
                .findFirst()
                .ifPresentOrElse(
                        summary -> {
                            long newDuration = summary.getTrainingSummaryDuration() - request.trainingDuration();
                            summary.setTrainingSummaryDuration(Math.max(0, newDuration));

                            log.info("DELETE: trainer='{}', year={}, month={}, subtracted={} min, total={} min",
                                    request.trainerUsername(), year, month,
                                    request.trainingDuration(), summary.getTrainingSummaryDuration());
                        },
                        () -> log.warn("DELETE no-op: no summary for trainer='{}', year={}, month={}",
                                request.trainerUsername(), year, month)
                );

        workloadRepository.save(workload);
    }

    private MonthlySummary findOrCreateMonthlySummary(TrainerWorkload workload, int year, int month) {
        return workload.getMonthlySummaries().stream()
                .filter(s -> s.getYear() == year && s.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthlySummary summary = new MonthlySummary(workload, year, month, 0);
                    workload.getMonthlySummaries().add(summary);
                    return summary;
                });
    }

    private WorkloadResponse toResponse(TrainerWorkload workload) {
        Map<Integer, List<MonthlySummary>> byYear = workload.getMonthlySummaries().stream()
                .collect(Collectors.groupingBy(
                        MonthlySummary::getYear,
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<WorkloadResponse.YearSummary> years = byYear.entrySet().stream()
                .map(entry -> new WorkloadResponse.YearSummary(
                        entry.getKey(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparingInt(MonthlySummary::getMonth))
                                .map(s -> new WorkloadResponse.MonthSummary(s.getMonth(), s.getTrainingSummaryDuration()))
                                .toList()
                ))
                .toList();

        return new WorkloadResponse(
                workload.getUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                workload.isActive(),
                years
        );
    }
}
