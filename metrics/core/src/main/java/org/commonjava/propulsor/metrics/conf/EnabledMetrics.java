package org.commonjava.propulsor.metrics.conf;

import org.commonjava.propulsor.config.ConfigurationException;
import org.commonjava.propulsor.config.section.ConfigurationSectionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
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
public abstract class EnabledMetrics<T>
        implements ConfigurationSectionListener<T>
{
    public static final String ENABLED = "enabled";

    private Map<String, Boolean> enabledMetricMap = new HashMap<>();

    private transient Map<String, Boolean> denormalized = new HashMap<>();

    public void set(String name, boolean enabled)
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
        Boolean enabled = enabledMetricMap.get( name );
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

    @Override
    public void sectionStarted( final String name )
            throws ConfigurationException
    {
        // NOP
    }

    @Override
    public void parameter( final String name, final String value )
            throws ConfigurationException
    {
        String enabledPrefix = getEnabledPrefix();
        if ( enabledPrefix == null )
        {
            enabledPrefix = "";
        }

        if ( ENABLED.equals( name ) )
        {
            this.enabled = Boolean.parseBoolean( value );
        }
        else if ( name.startsWith( enabledPrefix ) && name.endsWith( ENABLED ) && name.length() > (
                enabledPrefix.length() + ENABLED.length() + 1 ) )
        {
            String trimmed = name.substring( enabledPrefix.length(), name.length() - ENABLED.length() - 1 );
            set( trimmed, Boolean.valueOf( value ) );
        }
        else
        {
            handleParam( name, value );
        }
    }

    @Override
    public void sectionComplete( final String name )
            throws ConfigurationException
    {
        // NOP
    }

    protected abstract String getEnabledPrefix();

    protected abstract void handleParam( String name, String value );

//    protected void handleParam( String name, String value )
//    {
//        // NOP
//    }
}
