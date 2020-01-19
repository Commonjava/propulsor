/**
 * Copyright (C) 2015 John Casey (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.propulsor.metrics;

import org.commonjava.cdi.util.weft.ThreadContext;
import org.commonjava.propulsor.metrics.annotation.MetricNamed;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.cumulative.CumulativeTimingContextWrapper;
import org.commonjava.propulsor.metrics.spi.MetricsProvider;
import org.commonjava.propulsor.metrics.spi.TimingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.util.Set;
import java.util.function.Supplier;

import static com.codahale.metrics.MetricRegistry.name;
import static org.apache.commons.lang3.ClassUtils.getAbbreviatedName;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.commonjava.propulsor.metrics.MetricsConstants.CUMULATIVELY_METERED;
import static org.commonjava.propulsor.metrics.MetricsConstants.DEFAULT;
import static org.commonjava.propulsor.metrics.MetricsConstants.EXCEPTION;
import static org.commonjava.propulsor.metrics.MetricsConstants.SKIP_METRIC;
import static org.commonjava.propulsor.metrics.MetricsConstants.TIMER;

/**
 * Manager class that it responsible for orchestrating initialization of the metrics / health check registries.
 */
@ApplicationScoped
public class MetricsManager
{

    private static final Logger logger = LoggerFactory.getLogger( MetricsManager.class );

    private final MetricsProvider metricsProvider;

    private final MetricsConfig config;

    @Inject
    public MetricsManager( MetricsProvider provider,
                           MetricsConfig config )
    {
        this.metricsProvider = provider;
        this.config = config;

        if ( !config.isEnabled() )
        {
            logger.info( "Metrics subsystem is not enabled" );
            return;
        }

        logger.info( "Init metrics subsystem..." );
    }

    public void mark( Set<String> names )
    {
        metricsProvider.mark( names );
    }

    public void mark( String... names )
    {
        metricsProvider.mark( names );
    }

    public TimingContext time( final Set<String> timerNames )
    {
        return metricsProvider.time( timerNames );
    }

    public TimingContext time( final String... timerNames )
    {
        return metricsProvider.time( timerNames );
    }

    public <T> T wrapWithStandardMetrics( final Supplier<T> method, final Supplier<String> classifier )
    {
        String name = classifier.get();
        if ( !isMeteredCumulatively() || SKIP_METRIC.equals( name ) )
        {
            return method.get();
        }

        String nodePrefix = config.getInstancePrefix();

        String metricName = name( nodePrefix, name );
        String startName = name( metricName, "starts"  );

        String timerName = name( metricName, TIMER );
        String errorName = name( name, EXCEPTION );
        String eClassName = null;

        TimingContext timingContext = metricsProvider.time( timerName );
        if ( isMeteredCumulatively() )
        {
            timingContext = new CumulativeTimingContextWrapper( timingContext, metricName );
        }

        timingContext.start();

        logger.trace( "START: {}", metricName );

        try
        {
            metricsProvider.mark( startName );

            return method.get();
        }
        catch ( Throwable e )
        {
            eClassName = name( name, EXCEPTION, e.getClass().getSimpleName() );
            metricsProvider.mark( errorName, eClassName );

            throw e;
        }
        finally
        {
            timingContext.stop();
            metricsProvider.mark( metricName );
        }
    }

    public boolean isMeteredCumulatively()
    {
        return isMeteredCumulatively( null );
    }

    public boolean isMeteredCumulatively( ThreadContext ctx )
    {
        if ( ctx == null )
        {
            ctx = ThreadContext.getContext( false );
        }

        return ( ctx == null || ((Boolean) ctx.getOrDefault( CUMULATIVELY_METERED, Boolean.TRUE ) ) );
    }

    /**
     * Get default metric name. Use abbreviated package name, e.g., foo.bar.ClassA.methodB -> f.b.ClassA.methodB
     */
    public String getDefaultName( InvocationContext context )
    {
        // minimum len 1 shortens the package name and keeps class name
        String cls = getAbbreviatedName( context.getMethod().getDeclaringClass().getName(), 1 );
        String method = context.getMethod().getName();
        return name( cls, method );
    }

    /**
     * Get the metric fullname.
     * @param named user specified name
     * @param defaultName 'class name + method name', not null.
     */
    public String getName( String instancePrefix, MetricNamed named, String defaultName, String suffix )
    {
        String name = named.value();
        if ( isBlank( name ) || name.equals( DEFAULT ) )
        {
            name = defaultName;
        }
        return name( instancePrefix, name, suffix );
    }

    public TimingContext timeAll( final Set<String> timerNames )
    {
        return metricsProvider.time( timerNames );
    }
}
