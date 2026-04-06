package dev.ilkersahin.java.spring.gym.workload.service;

import dev.ilkersahin.java.spring.gym.workload.dto.ActionType;
import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadRequest;
import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadResponse;
import dev.ilkersahin.java.spring.gym.workload.exception.TrainerNotFoundException;
import dev.ilkersahin.java.spring.gym.workload.model.MonthlySummary;
import dev.ilkersahin.java.spring.gym.workload.model.TrainerWorkload;
import dev.ilkersahin.java.spring.gym.workload.model.YearSummary;
import dev.ilkersahin.java.spring.gym.workload.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceImpl implements WorkloadService {
    private final TrainerWorkloadRepository workloadRepository;

    @Override
    public void processWorkload(WorkloadRequest request) {
        log.info("Processing workload: trainer='{}', action={}, date={}, duration={}",
                request.trainerUsername(), request.actionType(),
                request.trainingDate(), request.trainingDuration());

        int year = request.trainingDate().getYear();
        int month = request.trainingDate().getMonthValue();

        if (request.actionType() == ActionType.ADD) {
            handleAdd(request, year, month);
        } else {
            handleDelete(request, year, month);
        }
    }

    @Override
    public WorkloadResponse getTrainerWorkload(String username) {
        log.info("Fetching workload for trainer '{}'", username);

        TrainerWorkload workload = workloadRepository.findById(username)
                .orElseThrow(() -> new TrainerNotFoundException(
                        "Trainer workload not found for username: " + username
                ));
        return toResponse(workload);
    }

    private void handleAdd(WorkloadRequest request, int year, int month) {
        TrainerWorkload workload = workloadRepository.findById(request.trainerUsername())
                .orElseGet(() -> {
                    log.info("ADD: creating new trainer document for '{}'", request.trainerUsername());
                    return new TrainerWorkload(
                            request.trainerUsername(),
                            request.trainerFirstName(),
                            request.trainerLastName(),
                            request.isActive()
                    );
                });

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

        YearSummary yearSummary = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElse(null);

        if (yearSummary == null) {
            log.warn("DELETE no-op: no data for trainer='{}', year={}", request.trainerUsername(), year);
            return;
        }

        MonthlySummary monthlySummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElse(null);

        if (monthlySummary == null) {
            log.warn("DELETE no-op: no data for trainer='{}', year={}, month={}",
                    request.trainerUsername(), year, month);
            return;
        }

        long newDuration = monthlySummary.getTrainingSummaryDuration() - request.trainingDuration();
        monthlySummary.setTrainingSummaryDuration(Math.max(0, newDuration));

        workloadRepository.save(workload);

        log.info("DELETE: trainer='{}', year={}, month={}, subtracted={} min, total={} min",
                request.trainerUsername(), year, month,
                request.trainingDuration(), monthlySummary.getTrainingSummaryDuration());
    }

    private MonthlySummary findOrCreateMonthlySummary(TrainerWorkload workload, int year, int month) {
        YearSummary yearSummary = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("Creating year entry {} for trainer '{}'", year, workload.getUsername());
                    YearSummary ys = new YearSummary(year);
                    workload.getYears().add(ys);
                    return ys;
                });

        return yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("Creating month entry {}/{} for trainer '{}'",
                            year, month, workload.getUsername());
                    MonthlySummary ms = new MonthlySummary(month, 0);
                    yearSummary.getMonths().add(ms);
                    return ms;
                });
    }

    private WorkloadResponse toResponse(TrainerWorkload workload) {
        var years = workload.getYears().stream()
                .sorted(Comparator.comparingInt(YearSummary::getYear))
                .map(ys -> new WorkloadResponse.YearSummary(
                        ys.getYear(),
                        ys.getMonths().stream()
                                .sorted(Comparator.comparingInt(MonthlySummary::getMonth))
                                .map(ms -> new WorkloadResponse.MonthSummary(
                                        ms.getMonth(),
                                        ms.getTrainingSummaryDuration()
                                ))
                                .toList()
                ))
                .toList();

        return new WorkloadResponse(
                workload.getUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                workload.getActive(),
                years
        );
    }
}
