/*
  Copyright (C) 2011-2017 Red Hat, Inc. (https://github.com/Commonjava/propulsor)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.commonjava.propulsor.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import org.commonjava.propulsor.metrics.annotation.Measure;
import org.commonjava.propulsor.metrics.annotation.MetricNamed;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codahale.metrics.MetricRegistry.name;
import static org.apache.commons.lang3.ClassUtils.getAbbreviatedName;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.commonjava.propulsor.metrics.annotation.MetricNamed.DEFAULT;
import static org.commonjava.propulsor.metrics.MetricsConstants.EXCEPTION;
import static org.commonjava.propulsor.metrics.MetricsConstants.METER;
import static org.commonjava.propulsor.metrics.MetricsConstants.TIMER;

@Interceptor
@Measure
public class MetricsInterceptor
{

    private static final Logger logger = LoggerFactory.getLogger( MetricsInterceptor.class );

    private final MetricsManager metricsManager;

    private final MetricsConfig config;

    @Inject
    public MetricsInterceptor( MetricsManager manager, MetricsConfig config )
    {
        this.metricsManager = manager;
        this.config = config;
    }

    @AroundInvoke
    public Object operation( InvocationContext context ) throws Exception
    {
        if ( !config.isEnabled() )
        {
            return context.proceed();
        }

        Method method = context.getMethod();
        Measure measure = method.getAnnotation( Measure.class );
        if ( measure == null )
        {
            measure = method.getDeclaringClass().getAnnotation( Measure.class );
        }

        logger.trace( "Gathering metrics for: {}", context.getContextData() );
        String nodePrefix = config.getInstancePrefix();

        String defaultName = getDefaultName( context );

        List<Timer.Context> timers = Stream.of( measure.timers() ).map( named ->
                                            {
                                                String name = getName( nodePrefix, named, defaultName, TIMER );
                                                Timer.Context tc = metricsManager.getTimer( name ).time();
                                                logger.trace( "START: {} ({})", name, tc );
                                                return tc;
                                            } )
                                           .collect( Collectors.toList() );

        try
        {
            return context.proceed();
        }
        catch ( Exception e )
        {
            Stream.of( measure.exceptions() ).forEach( ( named ) ->
                                           {
                                               String name = getName( nodePrefix, named, defaultName, EXCEPTION );
                                               Meter meter = metricsManager.getMeter( name );
                                               logger.trace( "ERRORS++ {}", name );
                                               meter.mark();
                                           } );

            throw e;
        }
        finally
        {
            if ( timers != null )
            {
                timers.forEach( timer->{
                    logger.trace( "STOP: {}", timer );
                    timer.stop();
                } );

            }
            Stream.of( measure.meters() ).forEach( ( named ) ->
                                           {
                                               String name = getName( nodePrefix, named, defaultName, METER );
                                               Meter meter = metricsManager.getMeter( name );
                                               logger.trace( "CALLS++ {}", name );
                                               meter.mark();
                                           } );
        }
    }

    /**
     * Get default metric name. Use abbreviated package name, e.g., foo.bar.ClassA.methodB -> f.b.ClassA.methodB
     */
    private String getDefaultName( InvocationContext context )
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
    private String getName( String instancePrefix, MetricNamed named, String defaultName, String suffix )
    {
        String name = named.value();
        if ( isBlank( name ) || name.equals( DEFAULT ) )
        {
            name = defaultName;
        }
        return name( instancePrefix, name, suffix );
    }

}
