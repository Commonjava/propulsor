package org.commonjava.propulsor.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.propulsor.metrics.conf.EnabledMetrics;

/**
 * Utility methods to make initializing metrics in a registry simpler and more consistent.
 */
public final class InitializerUtil
{
    private InitializerUtil()
    {
    }

    public static void registerIfEnabled( String key, Metric metric, EnabledMetrics<?> enabledMetrics,
                                          MetricRegistry registry )
    {
        if ( enabledMetrics.isEnabled() && enabledMetrics.isEnabled( key ) )
        {
            registry.register( key, metric );
        }
    }

    public static void registerIfEnabled( String key, HealthCheck healthCheck, EnabledMetrics<?> enabledMetrics,
                                          HealthCheckRegistry healthCheckRegistry )
    {
        if ( enabledMetrics.isEnabled() && enabledMetrics.isEnabled( key ) )
        {
            healthCheckRegistry.register( key, healthCheck );
        }
    }
}
