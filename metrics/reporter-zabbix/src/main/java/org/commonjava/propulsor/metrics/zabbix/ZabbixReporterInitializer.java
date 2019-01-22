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
package org.commonjava.propulsor.metrics.zabbix;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.spi.EnabledMetricFilter;
import org.commonjava.propulsor.metrics.spi.MetricsInitializer;
import org.commonjava.propulsor.metrics.zabbix.conf.ZabbixReporterConfig;
import org.commonjava.propulsor.metrics.zabbix.socket.ZabbixJsonRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jdcasey on 4/3/18.
 */
@ApplicationScoped
public class ZabbixReporterInitializer
                implements MetricsInitializer
{
    private ZabbixJsonRpcClient jsonRpcClient;

    private ScheduledExecutorService executor;

    private ZabbixReporterConfig zabbixConfig;

    private MetricsConfig metricsConfig;

    @Inject
    public ZabbixReporterInitializer( ZabbixJsonRpcClient jsonRpcClient, ZabbixReporterConfig zabbixConfig,
                                      MetricsConfig metricsConfig, ScheduledExecutorService executor )
    {
        this.jsonRpcClient = jsonRpcClient;
        this.zabbixConfig = zabbixConfig;
        this.metricsConfig = metricsConfig;
        this.executor = executor;
    }

    @Override
    public void initialize( final MetricRegistry registry, final HealthCheckRegistry healthCheckRegistry )
                    throws IOException, ZabbixConfigurationException
    {
        if ( metricsConfig.isEnabled() )
        {
            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.debug( "Setting up Zabbix metrics reporter" );

            ZabbixReporter reporter =
                            new ZabbixReporter( jsonRpcClient, registry, new EnabledMetricFilter( zabbixConfig ), executor,
                                                zabbixConfig, metricsConfig );

            reporter.start();
        }
    }
}
