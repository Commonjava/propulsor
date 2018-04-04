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
package org.commonjava.propulsor.metrics.reporter;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.commonjava.indy.metrics.conf.IndyMetricsConfig;
import org.commonjava.indy.metrics.zabbix.reporter.IndyZabbixReporter;
import org.commonjava.indy.metrics.zabbix.sender.IndyZabbixSender;
import org.commonjava.propulsor.metrics.MetricsNamed;
import org.commonjava.propulsor.metrics.conf.MetricsReportersConfig;
import org.elasticsearch.metrics.ElasticsearchReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiabai on 3/3/17.
 */
@ApplicationScoped
public class BrokenReporterIntializer
{
    private final static String FILTER_SIMPLE = "org.commonjava.indy";

    private final static String FILTER_JVM = "jvm";

    private final static String FILTER_HEALTHCHECK = "healthcheck";

    public final static String INDY_METRICS_REPORTER_GRPHITEREPORTER = "graphite";

    public final static String INDY_METRICS_REPORTER_CONSOLEREPORTER = "console";

    public final static String INDY_METRICS_REPORTER_ZABBIXREPORTER = "zabbix";

    public final static String INDY_METRICS_REPORTER_ELKEPORTER = "elasticsearch";

//    @Inject
//    IndyHttpProvider indyHttpProvider;
//
//    @Inject
//    ZabbixCacheStorage cache;

    @Inject
    @MetricsNamed
    MetricsReportersConfig config;

    public void initReporter( MetricRegistry metrics ) throws Exception
    {

        if ( !config.isReporterEnabled() )
        {
            initConsoleReporter( metrics, config );
            return;
        }
        if ( this.isExistReporter( INDY_METRICS_REPORTER_GRPHITEREPORTER ) )
        {
            initGraphiteReporterForSimpleMetric( metrics, config );
            initGraphiteReporterForJVMMetric( metrics, config );
            initGraphiteReporterForHealthCheckMetric( metrics, config );
        }

        if ( this.isExistReporter( INDY_METRICS_REPORTER_ZABBIXREPORTER ) )
        {
            this.initZabbixReporterForHealthCheckMetric( metrics, config );
            this.initZabbixReporterForJVMMetric( metrics, config );
            this.initZabbixReporterForSimpleMetric( metrics, config );
        }

        if ( this.isExistReporter( INDY_METRICS_REPORTER_CONSOLEREPORTER ) )
        {
            initConsoleReporter( metrics, config );
        }

        if ( this.isExistReporter( INDY_METRICS_REPORTER_ELKEPORTER ) )
        {
            initELKReporterForSimpleMetric( metrics, config );
            initELKReporterForJVMMetric( metrics, config );
            initELKReporterForHealthCheckMetric( metrics, config );
        }
    }

    private void initELKReporterForSimpleMetric( MetricRegistry metrics, MetricsReportersConfig config ) throws IOException
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.info( "Setting up Elasticsearch reporter for Indy metrics" );
        ElasticsearchReporter reporter = ElasticsearchReporter.forRegistry( metrics )
                                                              .hosts( config.getElkHosts().split( ";" ) )
                                                              .index( config.getElkIndex() )
                                                              .indexDateFormat( "YYYY-MM-dd" )
                                                              .filter( ( name, metric ) ->
                                                                       {
                                                                           if ( name.contains( FILTER_SIMPLE ) )
                                                                           {
                                                                               return true;
                                                                           }
                                                                           return false;
                                                                       } )
                                                              .build();

        reporter.start( config.getElkSimplePriod(), TimeUnit.SECONDS );
    }

    private void initELKReporterForJVMMetric( MetricRegistry metrics, IndyMetricsConfig config ) throws IOException
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.info( "Setting up Elasticsearch reporter for JVM metrics" );
        ElasticsearchReporter reporter = ElasticsearchReporter.forRegistry( metrics )
                                                              .hosts( config.getElkHosts().split( ";" ) )
                                                              .index( config.getElkIndex() )
                                                              .indexDateFormat( "YYYY-MM-dd" )
                                                              .filter( ( name, metric ) ->
                                                                       {
                                                                           if ( !name.contains( FILTER_SIMPLE )
                                                                                           && name.contains(
                                                                                           FILTER_JVM ) )
                                                                           {
                                                                               return true;
                                                                           }
                                                                           return false;
                                                                       } )
                                                              .build();

        reporter.start( config.getElkJVMPriod(), TimeUnit.SECONDS );
    }

    private void initELKReporterForHealthCheckMetric( MetricRegistry metrics, IndyMetricsConfig config )
                    throws IOException
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.info( "Setting up Elasticsearch reporter for Health Check metrics" );
        ElasticsearchReporter reporter = ElasticsearchReporter.forRegistry( metrics )
                                                              .hosts( config.getElkHosts().split( ";" ) )
                                                              .index( config.getElkIndex() )
                                                              .indexDateFormat( "YYYY-MM-dd" )
                                                              .filter( ( name, metric ) ->
                                                                       {
                                                                           if ( !name.contains( FILTER_SIMPLE )
                                                                                           && name.contains(
                                                                                           FILTER_HEALTHCHECK ) )
                                                                           {
                                                                               return true;
                                                                           }
                                                                           return false;
                                                                       } )
                                                              .build();

        reporter.start( config.getElkHealthCheckPriod(), TimeUnit.SECONDS );
    }

    private void initConsoleReporter( MetricRegistry metrics, IndyMetricsConfig config )
    {
        ConsoleReporter.forRegistry( metrics )
                       .build()
                       .start( IndyMetricsConfig.INDY_METRICS_REPORTER_GRPHITEREPORTER_DEFAULT_PERIOD,
                               TimeUnit.SECONDS );
    }

    private void initGraphiteReporterForSimpleMetric( MetricRegistry metrics, IndyMetricsConfig config )
    {
        final Graphite graphite =
                        new Graphite( new InetSocketAddress( config.getGrphiterHostName(), config.getGrphiterPort() ) );
        final GraphiteReporter reporter = GraphiteReporter.forRegistry( metrics )
                                                          .prefixedWith( config.getGrphiterPrefix() )
                                                          .convertRatesTo( TimeUnit.SECONDS )
                                                          .convertDurationsTo( TimeUnit.MILLISECONDS )
                                                          .filter( ( name, metric ) ->
                                                                   {
                                                                       if ( name.contains( FILTER_SIMPLE ) )
                                                                       {
                                                                           return true;
                                                                       }
                                                                       return false;
                                                                   } )
                                                          .build( graphite );
        reporter.start( config.getGrphiterSimplePriod(), TimeUnit.SECONDS );
    }

    private void initGraphiteReporterForJVMMetric( MetricRegistry metrics, IndyMetricsConfig config )
    {
        final Graphite graphite =
                        new Graphite( new InetSocketAddress( config.getGrphiterHostName(), config.getGrphiterPort() ) );
        final GraphiteReporter reporter = GraphiteReporter.forRegistry( metrics )
                                                          .prefixedWith( config.getGrphiterPrefix() )
                                                          .convertRatesTo( TimeUnit.SECONDS )
                                                          .convertDurationsTo( TimeUnit.MILLISECONDS )
                                                          .filter( ( name, metric ) ->
                                                                   {
                                                                       if ( !name.contains( FILTER_SIMPLE )
                                                                                       && name.contains( FILTER_JVM ) )
                                                                       {
                                                                           return true;
                                                                       }
                                                                       return false;
                                                                   } )
                                                          .build( graphite );
        reporter.start( config.getGrphiterJVMPriod(), TimeUnit.SECONDS );
    }

    private void initGraphiteReporterForHealthCheckMetric( MetricRegistry metrics, IndyMetricsConfig config )
    {
        final Graphite graphite =
                        new Graphite( new InetSocketAddress( config.getGrphiterHostName(), config.getGrphiterPort() ) );
        final GraphiteReporter reporter = GraphiteReporter.forRegistry( metrics )
                                                          .prefixedWith( config.getGrphiterPrefix() )
                                                          .convertRatesTo( TimeUnit.SECONDS )
                                                          .convertDurationsTo( TimeUnit.MILLISECONDS )
                                                          .filter( ( name, metric ) ->
                                                                   {
                                                                       if ( !name.contains( FILTER_SIMPLE )
                                                                                       && name.contains(
                                                                                       FILTER_HEALTHCHECK ) )
                                                                       {
                                                                           return true;
                                                                       }
                                                                       return false;
                                                                   } )
                                                          .build( graphite );
        reporter.start( config.getGrphiterHealthcheckPeriod(), TimeUnit.SECONDS );
    }

    private boolean isExistReporter( String reporter )
    {
        return config.getReporter().contains( reporter );

    }

}
