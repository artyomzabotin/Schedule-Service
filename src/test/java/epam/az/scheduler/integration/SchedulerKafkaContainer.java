package epam.az.scheduler.integration;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class SchedulerKafkaContainer extends KafkaContainer {

    private static final String KAFKA_IMAGE = "confluentinc/cp-kafka";
    private static final String KAFKA_VERSION = "5.3.4";

    public SchedulerKafkaContainer() {

        super(DockerImageName.parse(KAFKA_IMAGE + ":" + KAFKA_VERSION));
        start();
    }
}
