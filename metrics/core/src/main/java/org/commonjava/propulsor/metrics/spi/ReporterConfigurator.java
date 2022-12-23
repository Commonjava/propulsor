/**
 * Copyright (C) 2014-2022 Red Hat, Inc. (http://github.com/Commonjava/commonjava)
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
