package epam.az.scheduler.integration;

import com.jayway.restassured.RestAssured;
import epam.az.scheduler.SchedulerServiceApplication;
import epam.az.scheduler.dto.WorkerView;
import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import static com.jayway.restassured.RestAssured.given;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                SchedulerServiceApplication.class,
                KafkaTestConfig.class}
)
@DirtiesContext
@ActiveProfiles("kafka-test")
class KafkaIntegrationTest {

    @ClassRule
    public static SchedulerKafkaContainer container = new SchedulerKafkaContainer();

    @Autowired
    private KafkaTestConfig kafkaTestConfig;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {

        RestAssured.port = this.port;
        kafkaTestConfig.createTopics();
    }

    @Test
    void getActiveWorkers_kafkaBrokerExists_setOfWorkerViewsIsReceived() {

        // GIVEN


        // WHEN-THEN
        WorkerView[] workerViews = given()
                .when()
                .get("schedules/workers")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response()
                .getBody()
                .as(WorkerView[].class);

    }

}
