package com.dbtraining.reconx.observability;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * TICKET-ADV060 — KafkaHealthIndicator
 *
 * WHAT:    Custom HealthIndicator that checks Kafka connectivity using
 *          AdminClient and reports cluster information.
 *
 * HOW:     Creates an AdminClient with explicit request and API timeouts,
 *          calls describeCluster(), and exposes clusterId and nodeCount.
 *
 * WHY:     Allows Actuator to verify Kafka connectivity before consumers
 *          start processing events.
 *
 * OBSERVE: GET /actuator/health shows reconxKafka when Kafka is configured.
 * ============================================================================
 */
@Component("reconxKafka")
@ConditionalOnProperty(name = "reconx.kafka.health.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaHealthIndicator extends AbstractHealthIndicator {

    private final String bootstrapServers;

    public KafkaHealthIndicator(
            @Value("${spring.kafka.bootstrap-servers}")
            String bootstrapServers) {

        super("ReconX Kafka health check failed");
        this.bootstrapServers = bootstrapServers;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        Map<String, Object> cfg = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers,

                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG,
                2000,

                AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG,
                3000
        );

        try (AdminClient admin = AdminClient.create(cfg)) {

            DescribeClusterResult cluster = admin.describeCluster();

            String clusterId =
                    cluster.clusterId().get(2, TimeUnit.SECONDS);

            int nodeCount =
                    cluster.nodes().get(2, TimeUnit.SECONDS).size();

            builder.up()
                    .withDetail("clusterId", clusterId)
                    .withDetail("nodeCount", nodeCount);

        } catch (Exception e) {

            builder.down(e);
        }
    }
}
