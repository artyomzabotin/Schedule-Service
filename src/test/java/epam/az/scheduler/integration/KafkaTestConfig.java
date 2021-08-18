package epam.az.scheduler.integration;

import lombok.Getter;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.util.List;
import java.util.Map;

import static epam.az.scheduler.integration.KafkaIntegrationTest.container;

@Configuration
@Getter
@Profile("kafka-test")
public class KafkaTestConfig {

    final String topicReports = "workers-reports";
    final String topicWorkers = "active-workers";

    public void createTopics() {

        NewTopic newTopicReports = new NewTopic(topicReports, 1, (short) 1);
        NewTopic newTopicWorkers = new NewTopic(topicWorkers, 1, (short) 1);

        Map<String, Object> properties = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, container.getBootstrapServers()
        );

        try (AdminClient admin = AdminClient.create(properties)) {
            admin.createTopics(List.of(newTopicReports, newTopicWorkers));
        }
    }
}
