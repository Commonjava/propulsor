/**
 * Copyright (C) 2011-2017 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by xiabai on 2/27/17.
 *
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
    private HealthCheckRegistry healthCheckRegistry;

    @Inject
    private MetricRegistry metricRegistry;

    @PostConstruct
    public void init()
    {
        if ( !config.isEnabled() )
        {
            logger.debug( "Metrics subsystem is disabled." );
            return;
        }

        logger.debug( "Starting metrics subsystem..." );

        healthChecks.stream().forEach( hc -> healthCheckRegistry.register( hc.getName(), hc ) );
        initializers.stream().forEach( ( init)->init.initialize( metricRegistry, healthCheckRegistry ) );
    }

    public Timer getTimer( MetricNamed named )
    {
        logger.info( "call in IndyMetricsManager.getTimer from registry: {}", metricRegistry );
        return this.metricRegistry.timer( named.name() );
    }

    public Meter getMeter( MetricNamed named )
    {
        logger.info( "call in IndyMetricsManager.getMeter from registry: {}", metricRegistry );
        return metricRegistry.meter( named.name() );
    }

}
