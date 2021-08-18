package epam.az.scheduler.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import epam.az.scheduler.domain.Schedule;
import epam.az.scheduler.repository.ScheduleRepository;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.LocalDateTime;

class ScheduleControllerIntegrationTest extends IntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2020, 11, 11);
    private static final LocalDateTime CREATED = LocalDateTime.of(2020, 11, 11, 5, 0, 0);
    private static final String DESCRIPTION = "coding";
    private static final String TIMEZONE = "Europe/Paris";
    private static final Integer HOURS = 5;
    private static final Long WORKER_ID = 3L;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private Schedule schedule;


    @BeforeEach
    void setUp() {

        super.setUp();

        schedule = new Schedule();
        schedule.setWorkDescription(DESCRIPTION);
        schedule.setDate(DATE);
        schedule.setHours(HOURS);
        schedule.setCreatedAt(CREATED);
        schedule.setWorkerId(WORKER_ID);
        schedule.setTimezone(TIMEZONE);
    }

    @Test
    void getAllSchedules_schedulesExist_ListOfSchedulesIsReceived() {

        // GIVEN
        Schedule expected = scheduleRepository.save(schedule);

        // WHEN-THEN
        Schedule[] actual = given()
                .when()
                .get("/schedules")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response()
                .getBody()
                .as(Schedule[].class);

        assertThat(actual).hasSizeGreaterThanOrEqualTo(1);
        assertThat(actual).anyMatch(schedule -> schedule.equals(expected));
    }

    @Test
    void getById_scheduleExists_scheduleIsReceived() {

        // GIVEN
        Schedule expected = scheduleRepository.save(schedule);
        Long scheduleId = expected.getId();

        // WHEN-THEN
        Schedule actual = given()
                .when()
                .get("/schedules/"+ scheduleId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response()
                .getBody()
                .as(Schedule.class);

        assertThat(actual).isEqualTo(expected);
    }
}
