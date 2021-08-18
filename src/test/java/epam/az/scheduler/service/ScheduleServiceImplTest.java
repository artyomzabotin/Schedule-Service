package epam.az.scheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import epam.az.scheduler.domain.Schedule;
import epam.az.scheduler.dto.ReportView;
import epam.az.scheduler.dto.WorkerView;
import epam.az.scheduler.error.ErrorCode;
import epam.az.scheduler.error.ServiceErrorCode;
import epam.az.scheduler.exception.NotFoundException;
import epam.az.scheduler.repository.ScheduleRepository;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    private static final String GET_ACTIVE_WORKERS_API = "http://localhost:9001/workers/export";
    private static final Long SCHEDULE_ID = 1L;
    private static final Long WORKER_ID = 1L;
    private static final String TOPIC_WORKERS = "active-workers";
    private static final String TOPIC_REPORTS = "workers-reports";
    private static final String FULL_NAME = "Diego Maradona";
    private static final String POSITION = "CASHIER";
    private static final String SHOP = "Napoli";

    @Spy
    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private Schedule schedule;

    @Captor
    private ArgumentCaptor<Schedule> captor;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Consumer<String, WorkerView> workerConsumer;

    @Test
    void getById_scheduleExists_scheduleIsReceived() {

        // GIVEN
        Mockito.when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

        // WHEN
        Schedule actual = scheduleService.getById(SCHEDULE_ID);

        // THEN
        Mockito.verify(scheduleRepository).findById(SCHEDULE_ID);
        assertThat(actual).isEqualTo(schedule);
    }

    @Test
    void getById_scheduleDoesNotExist_notFoundExceptionIsThrown() {

        // GIVEN
        ErrorCode errorCode = ServiceErrorCode.SCHEDULE_NOT_FOUND;

        // WHEN
        Throwable throwable = catchThrowable(() -> scheduleService.getById(SCHEDULE_ID));

        // THEN
        assertThat(throwable).isInstanceOf(NotFoundException.class);
        NotFoundException notFoundException = (NotFoundException) throwable;
        assertThat(notFoundException.getErrorCode()).isEqualTo(errorCode);
        assertThat(notFoundException.getValues()).contains(SCHEDULE_ID);
    }

    @Test
    void getByWorkerId_reportsByWorkerExist_listOfSchedulesIsReceived() {

        // GIVEN
        Schedule secondSchedule = Mockito.mock(Schedule.class);
        List<Schedule> schedules = List.of(schedule, secondSchedule);
        Mockito.when(scheduleRepository.findAllByWorkerId(WORKER_ID)).thenReturn(schedules);

        // WHEN
        List<Schedule> actual = scheduleService.getByWorkerId(WORKER_ID);

        // THEN
        Mockito.verify(scheduleRepository).findAllByWorkerId(WORKER_ID);
        assertThat(actual).isEqualTo(schedules);
    }

    @Test
    void save_scheduleIsProvided_scheduleIsSaved() {

        // GIVEN
        Mockito.when(scheduleRepository.save(schedule)).thenReturn(schedule);

        // WHEN
        Schedule actual = scheduleService.save(schedule);

        // THEN
        Mockito.verify(scheduleRepository).save(schedule);
        assertThat(actual).isEqualTo(schedule);
    }

    @Test
    void getAll_schedulesDoesNotExist_emptyListIsReceived() {

        // WHEN
        List<Schedule> actual = scheduleService.getAll();

        // THEN
        Mockito.verify(scheduleRepository).findAll();
        assertThat(actual).isEqualTo(List.of());
    }

    @Test
    void getAll_schedulesExist_listOfSchedulesIsReceived() {

        // GIVEN
        List<Schedule> expected = List.of(schedule);
        Mockito.when(scheduleRepository.findAll()).thenReturn(expected);

        // WHEN
        List<Schedule> actual = scheduleService.getAll();

        // THEN
        Mockito.verify(scheduleRepository).findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getActiveWorkers_anyState_restCallIsMade() {

        // GIVEN
        Mockito.doReturn(Set.of()).when(scheduleService).consumeActiveWorkers();

        // WHEN
        scheduleService.callForActiveWorkers();

        // THEN
        Mockito.verify(restTemplate).getForObject(GET_ACTIVE_WORKERS_API, Void.class);
    }

    @Test
    void consumeActiveWorkers_kafkaBrokerExists_setOfWorkerViewsIsReceived() {

        // GIVEN
        WorkerView workerView = WorkerView.builder()
                .id(WORKER_ID)
                .fullName(FULL_NAME)
                .position(POSITION)
                .shop(SHOP)
                .build();

        Set<WorkerView> expected = Set.of(workerView);
        TopicPartition topicPartition = new TopicPartition(TOPIC_WORKERS, 0);

        ConsumerRecord<String, WorkerView> consumerRecord = new ConsumerRecord<>(TOPIC_WORKERS, 0, 0L, null, workerView);
        ConsumerRecords<String, WorkerView> consumerRecords = new ConsumerRecords<>(Map.of(topicPartition, List.of(consumerRecord)));

        Mockito.doReturn(consumerRecords).when(workerConsumer).poll(Duration.ofMillis(5000));

        // WHEN
        Set<WorkerView> actual = scheduleService.consumeActiveWorkers();

        // THEN
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getFullReportTable_kafkaBrokerExists_mapOfWorkerAndReportsIsReceived() {

        // GIVEN
        WorkerView workerView = WorkerView.builder()
                .id(WORKER_ID)
                .fullName(FULL_NAME)
                .position(POSITION)
                .shop(SHOP)
                .build();

        Schedule schedule = new Schedule();
        schedule.setWorkerId(WORKER_ID);
        schedule.setId(SCHEDULE_ID);
        List<Schedule> schedules = List.of(schedule);

        Map<WorkerView, List<Schedule>> expected = Map.of(workerView, schedules);
        Mockito.doReturn(Set.of(workerView)).when(scheduleService).callForActiveWorkers();
        Mockito.when(scheduleRepository.findAllByWorkerId(WORKER_ID)).thenReturn(schedules);

        // WHEN
        Map<WorkerView, List<Schedule>> actual = scheduleService.getFullReportTable();

        // THEN
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void consumeReports_kafkaBrokerExists_reportIsSaved() {

        // GIVEN
        LocalDate date = LocalDate.of(2021, 10, 4);
        String description = "coding";
        Integer hours = 8;
        String timezone = "Europe/Paris";
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2020, 10, 5, 11, 0, 0, 0, zoneId);
        String createdAt = zonedDateTime.toString();
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

        ReportView reportView = ReportView.builder()
                .workerId(WORKER_ID)
                .workDescription(description)
                .date(date)
                .hours(hours)
                .createdAt(createdAt)
                .build();

        ConsumerRecord<String, ReportView> consumerRecord =
                new ConsumerRecord<>(TOPIC_REPORTS, 0, 0L, WORKER_ID.toString(), reportView);

        // WHEN
        scheduleService.consumeReports(consumerRecord);

        // THEN
        Mockito.verify(scheduleRepository).save(captor.capture());
        Schedule result = captor.getValue();
        assertThat(result)
                .extracting(Schedule::getDate, Schedule::getHours, Schedule::getWorkDescription, Schedule::getWorkerId,
                        Schedule::getTimezone, Schedule::getCreatedAt)
                .containsExactly(date, hours, description, WORKER_ID, timezone, localDateTime);
    }
}
