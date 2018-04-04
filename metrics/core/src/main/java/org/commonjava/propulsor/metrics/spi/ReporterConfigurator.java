package org.commonjava.propulsor.metrics.spi;

import org.commonjava.propulsor.config.ConfigurationException;
import org.commonjava.propulsor.config.section.ConfigurationSectionListener;
import org.commonjava.propulsor.metrics.conf.EnabledMetrics;

/**
 * Reporters can configure themselves by implementing this.
 */
public abstract class ReporterConfigurator<T extends ReporterConfigurator>
        extends EnabledMetrics<T>
{
    @Override
    protected final String getEnabledPrefix()
    {
        return "m.";
    }
}
