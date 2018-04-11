package org.commonjava.propulsor.metrics.spi;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import org.commonjava.propulsor.metrics.conf.EnabledMetrics;

public class EnabledMetricFilter
    implements MetricFilter
{
    private EnabledMetrics<?> enabledMetrics;

    public EnabledMetricFilter( EnabledMetrics<?> enabledMetrics )
    {
        this.enabledMetrics = enabledMetrics;
    }

    @Override
    public boolean matches( String name, Metric metric )
    {
        return enabledMetrics.isEnabled( name );
    }
}
