/**
 * Copyright (C) 2014-2022 Red Hat, Inc. (http://github.com/Commonjava/commonjava)
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
package org.commonjava.propulsor.metrics.reporter;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.propulsor.metrics.ManagedMetricsException;
import org.commonjava.propulsor.metrics.conf.ConsoleReporterConfig;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.spi.MetricsInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
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
                    throws IOException, ManagedMetricsException
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
                           .build()
                           .start( config.getReportSeconds(), TimeUnit.SECONDS );
        }
    }
}
