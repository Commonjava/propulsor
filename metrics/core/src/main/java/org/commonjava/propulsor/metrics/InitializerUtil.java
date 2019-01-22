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
package org.commonjava.propulsor.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.propulsor.metrics.conf.EnabledMetrics;

/**
 * Utility methods to make initializing metrics in a registry simpler and more consistent.
 */
public final class InitializerUtil
{
    private InitializerUtil()
    {
    }

    public static void registerIfEnabled( String key, Metric metric, EnabledMetrics<?> enabledMetrics,
                                          MetricRegistry registry )
    {
        if ( enabledMetrics.isEnabled() && enabledMetrics.isEnabled( key ) )
        {
            registry.register( key, metric );
        }
    }

    public static void registerIfEnabled( String key, HealthCheck healthCheck, EnabledMetrics<?> enabledMetrics,
                                          HealthCheckRegistry healthCheckRegistry )
    {
        if ( enabledMetrics.isEnabled() && enabledMetrics.isEnabled( key ) )
        {
            healthCheckRegistry.register( key, healthCheck );
        }
    }
}
