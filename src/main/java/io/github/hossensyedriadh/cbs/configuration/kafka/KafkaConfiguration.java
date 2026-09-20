package io.github.hossensyedriadh.cbs.configuration.kafka;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfiguration {
    @Value("${kafka.bootstrap-servers}")
    private List<String> bootstrapServers;

    @Value("${kafka.ssl.truststore.path}")
    private String sslTruststorePath;

    @Value("${kafka.ssl.truststore.pwd}")
    private String sslTruststorePassword;

    @Value("${kafka.ssl.keystore.path}")
    private String sslKeystorePath;

    @Value("${kafka.ssl.keystore.pwd}")
    private String sslKeystorePassword;

    @Value("${kafka.ssl.key-pwd}")
    private String sslKeyPassword;

    @Value("${kafka.sasl.username}")
    private String kafkaUsername;

    @Value("${kafka.sasl.pwd}")
    private String kafkaPassword;

    @Value("${kafka.confluent.avro.schema-registry.url}")
    private String avroSchemaRegistryUrl;

    private Map<String, Object> baseConfig;

    @PostConstruct
    public void init() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        configProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, this.avroSchemaRegistryUrl);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.RETRIES_CONFIG, 5);
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30_000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120_000);
        configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60_000);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32_768);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33_554_432);
        configProps.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 1_000);
        configProps.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 10_000);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1_000);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        configProps.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        configProps.put(SaslConfigs.SASL_JAAS_CONFIG,
                String.format("org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";",
                        this.kafkaUsername, this.kafkaPassword));
        configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
        configProps.put(CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG, 300_000);
        configProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, this.sslTruststorePath);
        configProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, this.sslTruststorePassword);
        configProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, this.sslKeystorePath);
        configProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, this.sslKeystorePassword);
        configProps.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, this.sslKeyPassword);
        configProps.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.3");
        configProps.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2,TLSv1.3");
        configProps.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");

        this.baseConfig = Collections.unmodifiableMap(configProps);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(new HashMap<>(this.baseConfig));
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(this.producerFactory());
    }
}
