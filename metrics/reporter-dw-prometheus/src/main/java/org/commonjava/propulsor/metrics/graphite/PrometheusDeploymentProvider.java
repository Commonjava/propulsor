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
package org.commonjava.propulsor.metrics.graphite;

import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.MetricsServlet;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.dropwizard.servlet.DropwizardHealthCheckServletContextListener;
import org.commonjava.propulsor.metrics.graphite.conf.PrometheusReporterConfig;
import org.commonjava.propulsor.metrics.servlet.MetricServletProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
@Named
public class PrometheusDeploymentProvider
        implements MetricServletProvider
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private MetricsConfig config;

    private PrometheusReporterConfig reporterConfig;

    private MetricRegistry metricRegistry;

    @Inject
    public PrometheusDeploymentProvider( final MetricRegistry metricRegistry, final MetricsConfig config,
                                         final PrometheusReporterConfig reporterConfig )
    {
        this.config = config;
        this.reporterConfig = reporterConfig;
        this.metricRegistry = metricRegistry;
    }

    public DeploymentInfo get()
    {
        if ( !config.isEnabled() || !reporterConfig.isEnabled() )
        {
            return null;
        }

        CollectorRegistry.defaultRegistry.register( new DropwizardExports( metricRegistry, new DWPrometheusSampleBuilder( config.getInstancePrefix() ) ) );

        final ServletInfo servlet =
                Servlets.servlet( "prometheus-metrics", MetricsServlet.class ).addMapping( reporterConfig.getContextPath() );

        final DeploymentInfo di = new DeploymentInfo().addListener(
                Servlets.listener( DropwizardHealthCheckServletContextListener.class ) )
                                                      .setContextPath( reporterConfig.getContextPath() )
                                                      .addServlet( servlet )
                                                      .setDeploymentName( "Dropwizard-Prometheus Metrics Exporter" )
                                                      .setClassLoader( ClassLoader.getSystemClassLoader() );

        logger.info( "Returning deployment info for Prometheus metrics servlet" );
        return di;
    }
}
