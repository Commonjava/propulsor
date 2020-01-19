package org.commonjava.propulsor.metrics;

import java.util.Set;

public class MeteringContext
{
    private final Set<String> names;

    private final MetricsManager metricsManager;

    public MeteringContext( final Set<String> names, final MetricsManager metricsManager )
    {
        this.names = names;
        this.metricsManager = metricsManager;
    }

    public void mark()
    {
        metricsManager.mark( names );
    }

    public void mark(long count)
    {
        metricsManager.mark( names, count );
    }
}
