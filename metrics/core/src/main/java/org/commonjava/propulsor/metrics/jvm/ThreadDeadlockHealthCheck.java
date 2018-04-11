/**
 * Copyright (C) 2011-2017 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.propulsor.metrics.jvm;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.ThreadDeadlockDetector;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.healthcheck.ManagedHealthCheck;
import org.commonjava.propulsor.metrics.spi.MetricsInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

import static org.commonjava.propulsor.metrics.InitializerUtil.registerIfEnabled;

/**
 * Created by xiabai on 3/10/17.
 *
 * Detects thread deadlocks and reports them through the {@link HealthCheck} interface.
 */
@Named
@ApplicationScoped
public class ThreadDeadlockHealthCheck
        extends ManagedHealthCheck
        implements MetricsInitializer
{
    private static final Logger logger = LoggerFactory.getLogger( ThreadDeadlockHealthCheck.class );

    private static final String NAME = "jvm.thread.deadlock";

    private final MetricsConfig config;

    private final ThreadDeadlockDetector detector;

    @Inject
    public ThreadDeadlockHealthCheck( MetricsConfig config )
    {
        this.config = config;
        this.detector = new ThreadDeadlockDetector();
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public HealthCheck.Result check() throws Exception
    {
        final Set<String> threads = detector.getDeadlockedThreads();
        if ( threads.isEmpty() )
        {
            return HealthCheck.Result.healthy();
        }
        return HealthCheck.Result.unhealthy( threads.toString() );
    }

    @Override
    public void initialize( final MetricRegistry registry, final HealthCheckRegistry healthCheckRegistry )
    {
        registerIfEnabled( NAME, this, config, healthCheckRegistry );
    }
}
