/**
 * Copyright (C) 2011-2020 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.propulsor.metrics.dropwizard;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.propulsor.metrics.dropwizard.config.DropwizardConfig;
import org.commonjava.propulsor.metrics.dropwizard.spi.MetricsInitializer;
import org.commonjava.propulsor.metrics.spi.MetricsProvider;
import org.commonjava.propulsor.metrics.spi.TimingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by xiabai on 2/27/17. Adapted to propulsor by John Casey, 2020.
 */
@ApplicationScoped
public class DropwizardMetrics
        implements MetricsProvider
{

    public static final String METRIC_LOGGER_NAME = "org.commonjava.propulsor.metrics";

    private static final Logger logger = LoggerFactory.getLogger( METRIC_LOGGER_NAME );

    private MetricRegistry metricRegistry;

    private HealthCheckRegistry healthCheckRegistry;

    private DropwizardConfig config;

    @Inject
    public DropwizardMetrics( MetricRegistry metricRegistry, HealthCheckRegistry healthCheckRegistry,
                              DropwizardConfig dropwizardConfig, Instance<MetricsInitializer> metricsInitializers )
    {
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = healthCheckRegistry;
        this.config = dropwizardConfig;
        init( metricsInitializers );
    }

    public DropwizardMetrics( MetricRegistry metricRegistry, HealthCheckRegistry healthCheckRegistry,
                              DropwizardConfig dropwizardConfig, Set<MetricsInitializer> metricsInitializers )
    {
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = healthCheckRegistry;
        this.config = dropwizardConfig;
        init( metricsInitializers );
    }

    public void init( final Iterable<MetricsInitializer> metricsInitializers )
    {
        if ( !config.isEnabled() )
        {
            logger.info( "Metrics subsystem not enabled" );
            return;
        }

        logger.info( "Init metrics subsystem..." );

        metricsInitializers.forEach( mi -> mi.initialize( metricRegistry, healthCheckRegistry ) );
    }

    @Override
    public DropwizardTimingContext time( Set<String> timers )
    {
        Set<String> filteredTimers =
                timers.stream().filter( name -> config.isEnabled( name ) ).collect( Collectors.toSet() );

        return new DropwizardTimingContext( metricRegistry, filteredTimers );
    }

    @Override
    public TimingContext time( final String... timerNames )
    {
        return time( new HashSet<>( Arrays.asList( timerNames ) ) );
    }

    @Override
    public void mark( final Set<String> metricNames )
    {
        metricNames.stream()
                   .filter( name -> config.isEnabled( name ) )
                   .forEach( name -> metricRegistry.meter( name ).mark() );
    }

    @Override
    public void mark( final Set<String> metricNames, long count )
    {
        metricNames.stream()
                   .filter( name -> config.isEnabled( name ) )
                   .forEach( name -> metricRegistry.meter( name ).mark( count ) );
    }

    @Override
    public void registerGauges( final Map<String, Supplier<?>> gauges )
    {
        gauges.forEach( (name, supplier)->{
            Gauge<?> gauge = () -> supplier.get();
            metricRegistry.gauge( name, () -> gauge);
        } );
    }
}
