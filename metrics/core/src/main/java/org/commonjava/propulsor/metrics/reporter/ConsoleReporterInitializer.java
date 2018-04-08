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
