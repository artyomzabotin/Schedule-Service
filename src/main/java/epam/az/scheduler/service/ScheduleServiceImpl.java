package epam.az.scheduler.service;

import epam.az.scheduler.domain.Schedule;
import epam.az.scheduler.dto.ReportView;
import epam.az.scheduler.dto.WorkerView;
import epam.az.scheduler.error.ServiceErrorCode;
import epam.az.scheduler.exception.NotFoundException;
import epam.az.scheduler.repository.ScheduleRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private static final String REDIS_CACHE_VALUE = "schedule";
    private static final String GET_ACTIVE_WORKERS_API = "http://localhost:9001/workers/export";
    private static final String TOPIC_WORKERS = "active-workers";
    private static final String TOPIC_REPORTS = "workers-reports";
    private static final String GROUP_ID = "sephora";
    private static final String REPORT_CONTAINER_FACTORY = "reportContainerFactory";
    private final ScheduleRepository scheduleRepository;
    private final RestTemplate restTemplate;
    private final Consumer<String, WorkerView> workerConsumer;

    @Override
    @Cacheable(value = REDIS_CACHE_VALUE, key = "#id")
    public Schedule getById(Long id) {

        return scheduleRepository.findById(id).orElseThrow(() ->
                new NotFoundException(ServiceErrorCode.SCHEDULE_NOT_FOUND, id));
    }

    public List<Schedule> getByWorkerId(Long workerId) {

        return scheduleRepository.findAllByWorkerId(workerId);
    }

    public Schedule save(Schedule schedule) {

        return scheduleRepository.save(schedule);
    }

    @Override
    public List<Schedule> getAll() {

        return scheduleRepository.findAll();
    }

    @Override
    public Set<WorkerView> callForActiveWorkers() {

        restTemplate.getForObject(GET_ACTIVE_WORKERS_API, Void.class);

        return consumeActiveWorkers();
    }

    public Set<WorkerView> consumeActiveWorkers() {

        Set<WorkerView> activeWorkers = new HashSet<>();

        workerConsumer.subscribe(List.of(TOPIC_WORKERS));
        workerConsumer.seekToBeginning(workerConsumer.assignment());

        while (true) {
            ConsumerRecords<String, WorkerView> payload = workerConsumer.poll(Duration.ofMillis(5000));

            for (ConsumerRecord<String, WorkerView> record : payload) {
                WorkerView workerView = record.value();
                activeWorkers.add(workerView);
            }
            return activeWorkers;
        }
    }

    @Override
    public Map<WorkerView, List<Schedule>> getFullReportTable() {

        Map<WorkerView, List<Schedule>> mapping = new HashMap<>();
        Set<WorkerView> activeWorkers = callForActiveWorkers();

        for (WorkerView workerView: activeWorkers) {

            List<Schedule> schedules = getByWorkerId(workerView.getId());
            mapping.put(workerView, schedules);
        }

        return mapping;
    }

    @KafkaListener(topics = TOPIC_REPORTS, groupId = GROUP_ID, containerFactory = REPORT_CONTAINER_FACTORY)
    public void consumeReports(ConsumerRecord<String, ReportView> record) {

        log.info("Received record: {}", record);
        Schedule schedule = new Schedule();

        Long workerId = Long.valueOf(record.key());
        schedule.setWorkerId(workerId);

        ReportView reportView = record.value();

        schedule.setHours(reportView.getHours());

        String createdAtString = reportView.getCreatedAt();
        ZonedDateTime createdAt = ZonedDateTime.parse(createdAtString);
        ZoneId zoneId = createdAt.getZone();
        LocalDateTime localDateTime = createdAt.toLocalDateTime();

        schedule.setTimezone(zoneId.toString());
        schedule.setCreatedAt(localDateTime);
        schedule.setDate(reportView.getDate());

        schedule.setWorkDescription(reportView.getWorkDescription());

        save(schedule);
    }
}
