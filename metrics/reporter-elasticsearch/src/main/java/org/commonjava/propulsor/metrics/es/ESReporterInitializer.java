package org.commonjava.propulsor.metrics.es;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.es.conf.ESReporterConfig;
import org.commonjava.propulsor.metrics.spi.EnabledMetricFilter;
import org.commonjava.propulsor.metrics.spi.MetricsInitializer;
import org.elasticsearch.metrics.ElasticsearchReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Initialize a new Elasticsearch metrics reporter.
 */
@ApplicationScoped
public class ESReporterInitializer
                implements MetricsInitializer
{
    private ESReporterConfig config;

    private MetricsConfig metricsConfig;

    @Override
    public void initialize( MetricRegistry registry, HealthCheckRegistry healthCheckRegistry ) throws IOException
    {
        if ( config.isEnabled() )
        {
            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.debug( "Setting up Elasticsearch metrics reporter" );

            ElasticsearchReporter.forRegistry( registry )
                                 .hosts( config.getHosts().split( "(\\s|[,;])+" ) )
                                 .index( config.getIndexName() )
                                 .convertDurationsTo( MetricsConfig.DURATION_TIMEUNIT )
                                 .convertRatesTo( MetricsConfig.RATE_TIMEUNIT )
                                 .prefixedWith( metricsConfig.getInstancePrefix() )
                                 .timeout( config.getTimeout() )
                                 .indexDateFormat( config.getIndexDateFormat() )
                                 .filter( new EnabledMetricFilter( config ) )
                                 .build()
                                 .start( config.getReportSeconds(), SECONDS );
        }
    }

    @Inject
    public ESReporterInitializer( ESReporterConfig config, MetricsConfig metricsConfig )
    {
        this.config = config;
        this.metricsConfig = metricsConfig;
    }

}
