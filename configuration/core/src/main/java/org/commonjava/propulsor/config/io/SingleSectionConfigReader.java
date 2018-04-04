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
package org.commonjava.propulsor.config.io;

import static org.commonjava.propulsor.config.section.ConfigurationSectionListener.DEFAULT_SECTION;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.inject.Named;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.commonjava.propulsor.config.ConfigurationException;
import org.commonjava.propulsor.config.ConfigurationReader;
import org.commonjava.propulsor.config.ConfigurationRegistry;
import org.commonjava.propulsor.config.DefaultConfigurationRegistry;
import org.commonjava.propulsor.config.section.ConfigurationSectionListener;

@Named( "single-section" )
public class SingleSectionConfigReader
    implements ConfigurationReader
{

    private final ConfigurationRegistry dispatch;

    public SingleSectionConfigReader( final ConfigurationSectionListener<?> listener )
            throws ConfigurationException
    {
        dispatch = new DefaultConfigurationRegistry( listener );
    }

    public SingleSectionConfigReader( final Object target )
        throws ConfigurationException
    {
        dispatch = new DefaultConfigurationRegistry( target );
    }

    @Override
    public void loadConfiguration( final InputStream stream )
        throws ConfigurationException
    {
        loadConfiguration( stream, System.getProperties() );
    }

    @Override
    public void loadConfiguration( final InputStream stream, final Properties properties )
        throws ConfigurationException
    {
        final Properties props = new Properties();
        try
        {
            props.load( stream );
        }
        catch ( final IOException e )
        {
            throw new ConfigurationException( "Failed to read configuration. Error: %s", e, e.getMessage() );
        }

        if ( !dispatch.sectionStarted( DEFAULT_SECTION ) )
        {
            return;
        }

        final StringSearchInterpolator interp = new StringSearchInterpolator();
        interp.addValueSource( new PropertiesBasedValueSource( properties ) );

        for ( final Object k : props.keySet() )
        {
            final String key = (String) k;
            String value = props.getProperty( key );
            try
            {
                value = interp.interpolate( value );
            }
            catch ( final InterpolationException e )
            {
                throw new ConfigurationException( "Failed to resolve expressions in configuration '%s' (raw value: '%s'). Reason: %s", e, key, value,
                                                  e.getMessage() );
            }

            dispatch.parameter( DEFAULT_SECTION, key.trim(), value.trim() );
        }

        dispatch.sectionComplete( DEFAULT_SECTION );
        dispatch.configurationParsed();
    }

}
