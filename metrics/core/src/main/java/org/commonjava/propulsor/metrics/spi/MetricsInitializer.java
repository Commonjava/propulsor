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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.propulsor.metrics.ManagedMetricsException;

import java.io.IOException;

/**
 * Interface designed to inject a subclass of {@link ReporterConfigurator} and initialize a Metrics reporter based on
 * its configuration.
 */
public interface MetricsInitializer
{
    void initialize( MetricRegistry registry, HealthCheckRegistry healthCheckRegistry )
                    throws IOException, ManagedMetricsException;
}
