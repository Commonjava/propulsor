package org.commonjava.propulsor.metrics.spi;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public interface MetricsProvider
{
    TimingContext time( Set<String> timerNames );

    TimingContext time( String... timerNames );

    void mark( Set<String> metricNames );

    void mark( Set<String> names, long count );

    void registerGauges( Map<String, Supplier<?>> gauges );
}
