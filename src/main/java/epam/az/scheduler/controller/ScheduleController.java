package epam.az.scheduler.controller;

import epam.az.scheduler.domain.Schedule;
import epam.az.scheduler.dto.WorkerView;
import epam.az.scheduler.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Schedule> getAllSchedules() {

        return scheduleService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Schedule getById(@PathVariable("id") Long id) {

        return scheduleService.getById(id);
    }

    @GetMapping("/workers")
    @ResponseStatus(HttpStatus.OK)
    public Set<WorkerView> getActiveWorkers()  {

       return scheduleService.callForActiveWorkers();
    }

    @GetMapping("/table")
    @ResponseStatus(HttpStatus.OK)
    public Map<WorkerView, List<Schedule>> getFullReportTable() {

        return scheduleService.getFullReportTable();
    }
}
