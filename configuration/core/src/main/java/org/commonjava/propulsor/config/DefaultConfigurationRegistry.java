/**
 * Copyright (C) 2011 John Casey (jdcasey@commonjava.org)
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
package org.commonjava.propulsor.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.commonjava.propulsor.config.section.ConfigurationSectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigurationRegistry
    implements ConfigurationRegistry
{
    private final Collection<ConfigurationListener> listeners;

    private final Set<SectionConsumer> sectionConsumers;

    private Map<String, ConfigurationSectionListener<?>> sectionMap;

    public DefaultConfigurationRegistry( final Object... data )
        throws ConfigurationException
    {
        listeners = new ArrayList<ConfigurationListener>();
        sectionConsumers = new HashSet<SectionConsumer>();
        for ( final Object d : data )
        {
            withUnknownSomething( d );
        }
    }

    private void withUnknownSomething( Object d )
            throws ConfigurationException
    {
        if ( d instanceof Collection )
        {
            Collection<?> collection = (Collection<?>) d;
            for ( final Object o : collection )
            {
                withUnknownSomething( o );
            }
        }
        else if ( d instanceof SectionConsumer )
        {
            sectionConsumers.add( (SectionConsumer) d );
        }
        else if ( d instanceof ConfigurationListener )
        {
            with( (ConfigurationListener) d );
        }
        else if ( d instanceof ConfigurationSectionListener<?> )
        {
            with( (ConfigurationSectionListener<?>) d );
        }
        else if ( d instanceof Class<?> )
        {
            with( (Class<?>) d );
        }
        else
        {
            with( new DefaultConfigurationListener().with(d) );
        }
    }

    public DefaultConfigurationRegistry with( final SectionConsumer consumer )
    {
        sectionConsumers.add( consumer );
        return this;
    }

    public DefaultConfigurationRegistry with( final ConfigurationListener listener )
        throws ConfigurationException
    {
        listeners.add( listener );
        mapListener( listener );
        return this;
    }

    public DefaultConfigurationRegistry with( final ConfigurationSectionListener<?> listener )
        throws ConfigurationException
    {
        final DefaultConfigurationListener dcl = new DefaultConfigurationListener( listener );
        listeners.add( dcl );
        mapListener( dcl );
        return this;
    }

    public DefaultConfigurationRegistry with( final Class<?> type )
        throws ConfigurationException
    {
        final DefaultConfigurationListener dcl = new DefaultConfigurationListener( type );
        listeners.add( dcl );
        mapListener( dcl );
        return this;
    }

    @Override
    public void configurationParsed()
        throws ConfigurationException
    {
        if ( listeners != null )
        {
            for ( final ConfigurationListener listener : listeners )
            {
                listener.configurationComplete();
            }
        }
        else
        {
            // TODO: Log to debug level!
        }
    }

    @Override
    public boolean sectionStarted( final String name )
        throws ConfigurationException
    {
        final ConfigurationSectionListener<?> listener = sectionMap.get( name );
        if ( listener != null )
        {
            listener.sectionStarted( name );
            return true;
        }

        boolean process = false;
        for ( final SectionConsumer sectionConsumer : sectionConsumers )
        {
            sectionConsumer.sectionStarted( name );
            process = true;
        }

        return process;
    }

    @Override
    public void sectionComplete( final String name )
        throws ConfigurationException
    {
        final ConfigurationSectionListener<?> listener = sectionMap.get( name );
        if ( listener != null )
        {
            listener.sectionComplete( name );
        }

        for ( final SectionConsumer sectionConsumer : sectionConsumers )
        {
            sectionConsumer.sectionComplete( name );
        }

    }

    @Override
    public void parameter( final String section, final String name, final String value )
        throws ConfigurationException
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        final ConfigurationSectionListener<?> secListener = sectionMap.get( section );
        logger.trace( "Using listener: {} for section: {}", secListener, section );
        secListener.parameter( name, value );

        for ( final SectionConsumer sectionConsumer : sectionConsumers )
        {
            sectionConsumer.parameter( section, name, value );
        }

    }

    protected synchronized void mapSectionListeners()
        throws ConfigurationException
    {
        if ( listeners != null )
        {
            for ( final ConfigurationListener listener : listeners )
            {
                mapListener( listener );
            }
        }
        else
        {
            // TODO: Log to debug level!
        }
    }

    private void mapListener( final ConfigurationListener listener )
        throws ConfigurationException
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.debug( "Mapping configuration listener: {}", listener );
        if ( sectionMap == null )
        {
            sectionMap = new HashMap<String, ConfigurationSectionListener<?>>();
        }

        final Map<String, ConfigurationSectionListener<?>> parsers = listener.getSectionListeners();
        for ( final Map.Entry<String, ConfigurationSectionListener<?>> entry : parsers.entrySet() )
        {
            final String section = entry.getKey();
            logger.debug( "Attempting to map new section listener: {} with section name: {}", entry.getValue(), section );
            ConfigurationSectionListener<?> sectionListener = sectionMap.get( section );

            if ( sectionListener != null )
            {
                // check if it's the same instance coming in a different way...
                if ( sectionListener != entry.getValue())
                {
                    throw new ConfigurationException(
                            "Section collision! More than one ConfigurationParser bound to section: %s\n\t%s\n\t%s",
                            section, listener, entry.getValue() );
                }
            }
            else
            {
                sectionMap.put( section, entry.getValue() );
            }
        }
    }

}
