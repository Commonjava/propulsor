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
package org.commonjava.propulsor.metrics.dropwizard.jvm;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.dropwizard.spi.MetricsInitializer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.management.ManagementFactory;

import static org.commonjava.propulsor.metrics.InitializerUtil.registerIfEnabled;

/**
 * Created by xiabai on 3/10/17.
 */
@ApplicationScoped
@Named
public class JVMInitializer
                implements MetricsInitializer
{
    private static final String METRIC_JVM_MEMORY = "jvm.memory";

    private static final String METRIC_JVM_GARBAGE = "jvm.garbage";

    private static final String METRIC_JVM_THREADS = "jvm.threads";

    private static final String METRIC_JVM_FILES = "jvm.files";

    private static final String METRIC_JVM_BUFFERS = "jvm.buffers";

    private static final String METRIC_JVM_CLASSLOADING = "jvm.classloading";

    private final MetricsConfig config;

    @Inject
    public JVMInitializer( MetricsConfig config )
    {
        this.config = config;
    }

    @Override
    public void initialize( final MetricRegistry registry, final HealthCheckRegistry healthCheckRegistry )
    {
        registerIfEnabled( METRIC_JVM_MEMORY, new MemoryUsageGaugeSet(), config, registry );
        registerIfEnabled( METRIC_JVM_GARBAGE, new GarbageCollectorMetricSet(), config, registry );
        registerIfEnabled( METRIC_JVM_THREADS, new ThreadStatesGaugeSet(), config, registry );
        registerIfEnabled( METRIC_JVM_FILES, new FileDescriptorRatioGauge(), config, registry );
        registerIfEnabled( METRIC_JVM_CLASSLOADING, new ClassLoadingGaugeSet(), config, registry );
        registerIfEnabled( METRIC_JVM_BUFFERS, new BufferPoolMetricSet( ManagementFactory.getPlatformMBeanServer() ),
                           config, registry );
    }
}
