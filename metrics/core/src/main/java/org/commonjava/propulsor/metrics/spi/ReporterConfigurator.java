package org.commonjava.propulsor.metrics.spi;

import org.commonjava.propulsor.config.ConfigurationException;
import org.commonjava.propulsor.config.section.ConfigurationSectionListener;
import org.commonjava.propulsor.metrics.conf.EnabledMetrics;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;

/**
 * Reporters can configure themselves by implementing this.
 */
public abstract class ReporterConfigurator<T extends ReporterConfigurator>
        extends EnabledMetrics<T>
{
    private long reportPeriod;

    @Override
    protected final String getEnabledPrefix()
    {
        return "m.";
    }

    public long getReportSeconds()
    {
        return reportPeriod;
    }

    public void setReportPeriod( long reportPeriod )
    {
        this.reportPeriod = reportPeriod;
    }
}
