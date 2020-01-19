package org.commonjava.propulsor.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class DropwizardProducer
{
    @ApplicationScoped
    @Produces
    public MetricRegistry getMetricRegistry()
    {
        return new MetricRegistry();
    }

    @ApplicationScoped
    @Produces
    public HealthCheckRegistry getHealthCheckRegistry() {
        return new HealthCheckRegistry();
    }
}
