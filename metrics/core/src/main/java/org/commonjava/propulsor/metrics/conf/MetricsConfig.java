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
package org.commonjava.propulsor.metrics.conf;

import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

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

    public static final TimeUnit RATE_TIMEUNIT = TimeUnit.MILLISECONDS;

    public static final TimeUnit DURATION_TIMEUNIT = TimeUnit.SECONDS;

    private String instancePrefix;

    @Override
    protected String getEnabledPrefix()
    {
        return "";
    }

    public String getInstancePrefix()
    {
        return instancePrefix;
    }

    @ConfigName( "instance.prefix" )
    public void setInstancePrefix( final String instancePrefix )
    {
        this.instancePrefix = instancePrefix;
    }

}
