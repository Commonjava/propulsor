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
package org.commonjava.propulsor.metrics.spi;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import org.commonjava.propulsor.metrics.conf.EnabledMetrics;

public class EnabledMetricFilter
    implements MetricFilter
{
    private EnabledMetrics<?> enabledMetrics;

    public EnabledMetricFilter( EnabledMetrics<?> enabledMetrics )
    {
        this.enabledMetrics = enabledMetrics;
    }

    @Override
    public boolean matches( String name, Metric metric )
    {
        return enabledMetrics.isEnabled( name );
    }
}
