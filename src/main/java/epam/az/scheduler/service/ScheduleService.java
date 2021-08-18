package epam.az.scheduler.service;

import epam.az.scheduler.domain.Schedule;
import epam.az.scheduler.dto.WorkerView;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ScheduleService {

    Schedule getById(Long id);

    List<Schedule> getAll();

    Set<WorkerView> callForActiveWorkers();

    Map<WorkerView, List<Schedule>> getFullReportTable();
}
