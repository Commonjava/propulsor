package org.commonjava.propulsor.metrics.conf;

import org.commonjava.propulsor.config.annotation.SectionName;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * Globally turn on/off metrics by name hierarchy, or globally enable/disable the metrics system.
 */
@ApplicationScoped
@Named
@SectionName( MetricsConfig.SECTION_NAME )
public class MetricsConfig
        extends EnabledMetrics<MetricsConfig>
{
    public static final String SECTION_NAME = "metrics";

    private String instancePrefix;

    @Override
    public MetricsConfig getConfiguration()
    {
        return this;
    }

    @Override
    protected String getEnabledPrefix()
    {
        return "";
    }

    @Override
    protected void handleParam( final String name, final String value )
    {
        // NOP
    }

    public String getInstancePrefix()
    {
        return instancePrefix;
    }

    public void setInstancePrefix( final String instancePrefix )
    {
        this.instancePrefix = instancePrefix;
    }
}
