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
package org.commonjava.propulsor.metrics.dropwizard.console;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.dropwizard.config.ConsoleReporterConfig;
import org.commonjava.propulsor.metrics.dropwizard.spi.MetricsInitializer;
import org.commonjava.propulsor.metrics.dropwizard.spi.EnabledMetricFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static org.commonjava.propulsor.metrics.conf.MetricsConfig.DURATION_TIMEUNIT;
import static org.commonjava.propulsor.metrics.conf.MetricsConfig.RATE_TIMEUNIT;

@ApplicationScoped
public class ConsoleReporterInitializer
                implements MetricsInitializer
{
    private final ConsoleReporterConfig config;

    private final MetricsConfig metricsConfig;

    @Inject
    public ConsoleReporterInitializer( ConsoleReporterConfig config, MetricsConfig metricsConfig )
    {
        this.config = config;
        this.metricsConfig = metricsConfig;
    }

    @Override
    public void initialize( MetricRegistry registry, HealthCheckRegistry healthCheckRegistry )
    {
        if ( config.isEnabled() )
        {
            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.debug( "Setting up console metrics reporter" );

            ConsoleReporter.forRegistry( registry )
                           .convertDurationsTo( DURATION_TIMEUNIT )
                           .convertRatesTo( RATE_TIMEUNIT )
                           .formattedFor( config.getTimeZone() )
                           .formattedFor( config.getLocale() )
                           .filter( new EnabledMetricFilter( config ) )
                           .build()
                           .start( config.getReportSeconds(), TimeUnit.SECONDS );
        }
    }
}
