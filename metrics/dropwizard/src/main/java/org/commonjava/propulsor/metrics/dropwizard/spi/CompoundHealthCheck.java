package org.commonjava.propulsor.metrics.dropwizard.spi;

import com.codahale.metrics.health.HealthCheck;

import java.util.Map;

public interface CompoundHealthCheck
{
        Map<String, HealthCheck> getHealthChecks();
}
