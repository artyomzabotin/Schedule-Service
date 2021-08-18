package epam.az.scheduler.config;

import epam.az.scheduler.dto.ReportView;
import epam.az.scheduler.dto.WorkerView;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final String BOOTSTRAP_ADDRESS = "127.0.0.1:9092";
    private static final String GROUP_ID = "sephora";

    @Bean
    public ConsumerFactory<String, ReportView> reportConsumerFactory() {

        JsonDeserializer<ReportView> deserializer = new JsonDeserializer<>(ReportView.class);
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> properties = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_ADDRESS,
                ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer
        );

        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReportView> reportContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, ReportView> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(reportConsumerFactory());

        return factory;
    }

	@Bean("prototype")
    public Consumer<String, WorkerView> workerConsumer() {

        JsonDeserializer<WorkerView> deserializer = new JsonDeserializer<>(WorkerView.class);
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> properties = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_ADDRESS,
                ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer
        );

        return new KafkaConsumer<>(properties, new StringDeserializer(), deserializer);
    }
}
