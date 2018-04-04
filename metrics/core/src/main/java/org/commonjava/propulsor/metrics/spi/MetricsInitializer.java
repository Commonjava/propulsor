package org.commonjava.propulsor.metrics.spi;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.health.HealthCheckRegistry;

/**
 * Interface designed to inject a subclass of {@link ReporterConfigurator} and initialize a Metrics reporter based on
 * its configuration.
 */
public interface MetricsInitializer
{
    void initialize( MetricRegistry registry, HealthCheckRegistry healthCheckRegistry );
}
