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

import org.commonjava.propulsor.config.ConfigurationException;
import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.section.BeanSectionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;

/**
 * Mapping of enabled metrics. This works much the same way as normal logging systems in Java, where enablement is
 * hierarchical. You can turn on all metrics in org.commonjava.propulsor by setting org.commonjava.propulsor.enable=true,
 * while still turning off org.commonjava.propulsor.config.enable=false or even
 * org.commonjava.propulsor.config.DotConfConfigurationReader.enable=false.
 *
 * In practice, metrics will probably be topical, not arranged around class / package hierarchies. But the same logic
 * applies.
 */
public abstract class MetricSubsetConfig<T>
        extends BeanSectionListener<T>
{
    public static final String ENABLED = "enabled";

    private static final String ENABLED_METRIC_PREFIX = "m.";

    private Map<String, Boolean> enabledMetricMap = new HashMap<>();

    private transient Map<String, Boolean> denormalized = new HashMap<>();

    public void enableMetric( String name, boolean enabled)
    {
        enabledMetricMap.put( name, enabled );
    }

    public Boolean get( String name )
    {
        return enabledMetricMap.get( name );
    }

    public Boolean clear( String name )
    {
        return enabledMetricMap.remove( name );
    }

    public boolean isEnabled( String name )
    {
        Boolean enabled = this.enabled;
        if ( enabled != null )
        {
            return enabled;
        }

        enabled = enabledMetricMap.get( name );
        if ( enabled != null )
        {
            return enabled;
        }

        String[] parts = name.split("\\.");
        List<String> subnames = new ArrayList<>();
        final StringBuilder sb = new StringBuilder(name);

        // NOTE Ordering of add...we want to search most specific first!
        Stream.of(parts).forEach( (part)->{
            sb.append( '.' ).append( part );
            subnames.add( 0, sb.toString() );
        } );

        return subnames.stream().map( ( subname ) ->
                                         {
                                             Boolean result = denormalized.get( subname );
                                             if ( result == null )
                                             {
                                                 result = enabledMetricMap.get( subname );
                                                 if ( result != null )
                                                 {
                                                     denormalized.put( subname, result );
                                                 }
                                             }
                                             return result;
                                         } ).filter( Objects::nonNull ).findFirst().orElse( FALSE );
    }

    private boolean enabled;

    public boolean isEnabled()
    {
        return enabled;
    }

    @ConfigName( "enabled" )
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @ConfigName( BeanSectionListener.UNSET_PROPERTIES_MAP )
    public void setMapParameters( final Map<String, Object> params )
            throws ConfigurationException
    {
        params.forEach( (name,v)->{
            String value = v == null ? null : String.valueOf( v );

            if ( name.startsWith( ENABLED_METRIC_PREFIX ) && name.endsWith( ENABLED ) && name.length() > (
                            ENABLED_METRIC_PREFIX.length() + ENABLED.length() + 1 ) )
            {
                String trimmed = name.substring( ENABLED_METRIC_PREFIX.length(), name.length() - ENABLED.length() - 1 );
                enableMetric( trimmed, Boolean.valueOf( value ) );
            }
        } );
    }

    public final void computeEnabled( MetricsConfig config )
    {
        final MetricSubsetConfig<T> mine = this;
        config.getEnabledMetrics().forEach( (name,flag)->{
            mine.computeEnabledIfAbsent( name, n -> flag );
        } );
    }

    protected final Map<String, Boolean> getEnabledMetrics()
    {
        return enabledMetricMap;
    }

    public final void computeEnabledIfAbsent( String name, Function<String, Boolean> func )
    {
        enabledMetricMap.computeIfAbsent( name, func );
    }
}
