package org.commonjava.propulsor.metrics.cumulative;

import org.commonjava.cdi.util.weft.ThreadContext;
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

    private Set<String> names;

    private long start;

    public CumulativeTimingContextWrapper( TimingContext context, String... names )
    {
        this.context = context;
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

        names.forEach( name->{
            ThreadContext ctx = ThreadContext.getContext( true );
            if ( ctx != null )
            {
                ctx.putIfAbsent( CUMULATIVE_TIMINGS, new ConcurrentHashMap<>() );
                Map<String, Double> timingMap = (Map<String, Double>) ctx.get( CUMULATIVE_TIMINGS );

                timingMap.merge( name, elapsed, Double::sum );

                ctx.putIfAbsent( CUMULATIVE_COUNTS, new ConcurrentHashMap<>() );
                Map<String, Integer> countMap =
                        (Map<String, Integer>) ctx.get( CUMULATIVE_COUNTS );

                countMap.merge( name, 1, ( existingVal, newVal ) -> existingVal + 1 );
            }
        } );

        return results;
    }
}
