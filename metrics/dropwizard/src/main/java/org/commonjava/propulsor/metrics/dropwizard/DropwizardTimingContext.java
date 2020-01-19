package org.commonjava.propulsor.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.commonjava.propulsor.metrics.spi.TimingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class DropwizardTimingContext
        implements TimingContext
{
    private final MetricRegistry metricRegistry;

    private final Set<String> timerNames;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Set<Timer> timers;

    private Set<Timer.Context> started;

    public DropwizardTimingContext( final MetricRegistry metricRegistry, final Set<String> timerNames )
    {
        this.metricRegistry = metricRegistry;
        this.timerNames = timerNames;
        this.timers = timerNames.stream().map( name ->metricRegistry.timer( name ) )
                                           .collect( Collectors.toSet() );
    }

    public void start()
    {
        this.started = timers.stream().map(t->t.time()).collect( Collectors.toSet() );
    }

    public Set<Long> stop()
    {
        if ( started != null )
        {
            return started.stream().map( t->t.stop() ).collect( Collectors.toSet() );
        }

        return Collections.emptySet();
    }
}
