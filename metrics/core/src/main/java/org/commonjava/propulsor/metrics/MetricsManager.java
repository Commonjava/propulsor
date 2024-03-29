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
package org.commonjava.propulsor.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.healthcheck.ManagedHealthCheck;
import org.commonjava.propulsor.metrics.spi.MetricsInitializer;
import org.commonjava.propulsor.metrics.spi.ReporterConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Manager class that it responsible for orchestrating initialization of the metrics / health check registries.
 */
@ApplicationScoped
public class MetricsManager
{

    private static final Logger logger = LoggerFactory.getLogger( MetricsManager.class );

    @Inject
    private Instance<ManagedHealthCheck> healthChecks;

    @Inject
    private Instance<MetricsInitializer> initializers;

    @Inject
    private MetricsConfig config;

    @Inject
    private Instance<ReporterConfigurator<?>> reporterConfigurators;

    @Inject
    private HealthCheckRegistry healthCheckRegistry;

    @Inject
    private MetricRegistry metricRegistry;

    @PostConstruct
    public void init()
    {
        if ( !config.isEnabled() )
        {
            logger.info( "Indy metrics subsystem not enabled" );
            return;
        }

        logger.info( "Init metrics subsystem..." );

        if ( healthChecks != null )
        {
            healthChecks.forEach( hc -> healthCheckRegistry.register( hc.getName(), hc ) );
        }

        if ( initializers != null )
        {
            initializers.forEach( init -> {
                try
                {
                    init.initialize( metricRegistry, healthCheckRegistry );
                }
                catch ( IOException e )
                {
                    final Logger logger = LoggerFactory.getLogger( getClass() );
                    logger.error( String.format( "Failed to initialize metrics subsystem: %s. Error: %s", init,
                                                 e.getMessage() ), e );
                }
                catch ( ManagedMetricsException e )
                {
                    e.printStackTrace();
                }
            } );
        }

        if ( reporterConfigurators != null )
        {
            reporterConfigurators.forEach( rc -> rc.computeEnabled( config ) );
        }
    }

    public Timer getTimer( String name )
    {
        return this.metricRegistry.timer( name );
    }

    public Meter getMeter( String name )
    {
        return metricRegistry.meter( name );
    }
}
