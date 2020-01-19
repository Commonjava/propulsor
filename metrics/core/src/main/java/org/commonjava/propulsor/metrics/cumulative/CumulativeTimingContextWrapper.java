package org.commonjava.propulsor.metrics.cumulative;

import org.commonjava.cdi.util.weft.ThreadContext;
import org.commonjava.propulsor.metrics.MetricsManager;
import org.commonjava.propulsor.metrics.spi.TimingContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.commonjava.propulsor.metrics.MetricsConstants.CUMULATIVE_COUNTS;
import static org.commonjava.propulsor.metrics.MetricsConstants.CUMULATIVE_TIMINGS;
import static org.commonjava.propulsor.metrics.MetricsConstants.NANOS_PER_MILLISECOND;

public class CumulativeTimingContextWrapper
        implements TimingContext
{
    private final TimingContext context;

    private MetricsManager manager;

    private Set<String> names;

    private long start;

    public CumulativeTimingContextWrapper( TimingContext context, MetricsManager manager, String... names )
    {
        this.context = context;
        this.manager = manager;
        this.names = new HashSet<>( Arrays.asList( names ) );
    }

    @Override
    public void start()
    {
        context.start();
        start = System.nanoTime();
    }

    @Override
    public Set<Long> stop()
    {
        Set<Long> results = context.stop();


        double elapsed = (System.nanoTime() - start) / NANOS_PER_MILLISECOND;
        manager.accumulate( names, elapsed );

        return results;
    }
}
