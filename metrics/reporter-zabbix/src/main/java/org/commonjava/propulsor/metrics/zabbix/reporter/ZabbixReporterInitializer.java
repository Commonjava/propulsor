package org.commonjava.propulsor.metrics.zabbix.reporter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.spi.MetricsInitializer;

import java.util.concurrent.TimeUnit;

/**
 * Created by jdcasey on 4/3/18.
 */
public class ZabbixReporterInitializer
        implements MetricsInitializer
{
    @Override
    public void initialize( final MetricRegistry registry, final HealthCheckRegistry healthCheckRegistry )
    {
        ZabbixReporter reporter = initZabbixReporter( metrics, config ).filter( ( name, metric ) ->
                                                                                    {
                                                                                        if ( !name.contains(
                                                                                                FILTER_SIMPLE )
                                                                                                && name.contains(
                                                                                                FILTER_JVM ) )
                                                                                        {
                                                                                            return true;
                                                                                        }
                                                                                        return false;
                                                                                    } ).build( initZabbixSender() );

        reporter.start( config.getZabbixJVMPriod(), TimeUnit.SECONDS );
    }

    private void initZabbixReporterForSimpleMetric( MetricRegistry metrics, IndyMetricsConfig config )
    {
        ZabbixReporter reporter = initZabbixReporter( metrics, config ).filter( ( name, metric ) ->
                                                                                    {
                                                                                        if ( name.contains(
                                                                                                FILTER_SIMPLE ) )
                                                                                        {
                                                                                            return true;
                                                                                        }
                                                                                        return false;
                                                                                    } ).build( initZabbixSender() );

        reporter.start( config.getZabbixSimplePriod(), TimeUnit.SECONDS );
    }

    private void initZabbixReporterForJVMMetric( MetricRegistry metrics, IndyMetricsConfig config )
    {
        ZabbixReporter reporter = initZabbixReporter( metrics, config ).filter( ( name, metric ) ->
                                                                                    {
                                                                                        if ( !name.contains(
                                                                                                FILTER_SIMPLE )
                                                                                                && name.contains(
                                                                                                FILTER_JVM ) )
                                                                                        {
                                                                                            return true;
                                                                                        }
                                                                                        return false;
                                                                                    } ).build( initZabbixSender() );

        reporter.start( config.getZabbixJVMPriod(), TimeUnit.SECONDS );
    }

    private void initZabbixReporterForHealthCheckMetric( MetricRegistry metrics, IndyMetricsConfig config )
    {
        ZabbixReporter reporter = initZabbixReporter( metrics, config ).filter( ( name, metric ) ->
                                                                                    {
                                                                                        if ( !name.contains(
                                                                                                FILTER_SIMPLE )
                                                                                                && name.contains(
                                                                                                FILTER_HEALTHCHECK ) )
                                                                                        {
                                                                                            return true;
                                                                                        }
                                                                                        return false;
                                                                                    } ).build( initZabbixSender() );

        reporter.start( config.getZabbixHealthcheckPeriod(), TimeUnit.SECONDS );
    }

    private ZabbixReporter.Builder initZabbixReporter( MetricRegistry metrics, MetricsConfig config )
    {
        return ZabbixReporter.forRegistry( metrics )
                             .prefix( config.getZabbixPrefix() )
                             .convertRatesTo( TimeUnit.SECONDS )
                             .convertDurationsTo( TimeUnit.MILLISECONDS )
                             .hostName( config.getZabbixLocalHostName() );
    }

    private IndyZabbixSender initZabbixSender()
    {
        final IndyZabbixSender zabbixSender = IndyZabbixSender.create()
                                                              .zabbixHost( config.getZabbixHost() )
                                                              .zabbixPort( config.getZabbixPort() )
                                                              .zabbixHostUrl( config.getZabbixApiHostUrl() )
                                                              .zabbixUserName( config.getZabbixUser() )
                                                              .zabbixUserPwd( config.getZabbixPwd() )
                                                              .hostName( config.getZabbixLocalHostName() )
                                                              .bCreateNotExistZabbixSender( true )
                                                              .indyHttpProvider( indyHttpProvider )
                                                              .metricsZabbixCache( cache )
                                                              .build();
        return zabbixSender;
    }
}
