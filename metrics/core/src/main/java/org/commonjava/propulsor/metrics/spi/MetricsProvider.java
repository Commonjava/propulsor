package org.commonjava.propulsor.metrics.spi;

import java.util.Set;

public interface MetricsProvider
{
    TimingContext time( Set<String> timerNames );

    TimingContext time( String... timerNames );

    void mark( Set<String> metricNames );

    void mark( String... metricNames );
}
