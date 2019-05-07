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
package org.commonjava.propulsor.config.dotconf;

import static org.apache.commons.io.IOUtils.readLines;
import static org.commonjava.propulsor.config.ConfigUtils.loadStandardConfigProperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.commonjava.propulsor.config.*;
import org.commonjava.propulsor.config.section.ConfigurationSectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DotConfConfigurationReader
    implements ConfigurationReader
{

    private final ConfigurationRegistry dispatch;

    private final Pattern parameter;

    @Inject
    public DotConfConfigurationReader( final ConfigurationRegistry dispatch )
    {
        this.dispatch = dispatch;
        parameter = Pattern.compile( "\\s*([^#:=]+)\\s*[:=]\\s*([^#]+)(\\s*#.*)?" );
    }

    public DotConfConfigurationReader( final Class<?>... types )
        throws ConfigurationException
    {
        this( new DefaultConfigurationRegistry( new DefaultConfigurationListener( types ) ) );
    }

    public DotConfConfigurationReader( final ConfigurationSectionListener<?>... sectionListeners )
        throws ConfigurationException
    {
        this( new DefaultConfigurationRegistry( new DefaultConfigurationListener( sectionListeners ) ) );
    }

    public DotConfConfigurationReader( final ConfigurationListener... listeners )
        throws ConfigurationException
    {
        this( new DefaultConfigurationRegistry( listeners ) );
    }

    public DotConfConfigurationReader( final Collection<ConfigurationListener> listeners )
            throws ConfigurationException
    {
        this( new DefaultConfigurationRegistry( listeners ) );
    }

    public DotConfConfigurationReader( final Object... data )
        throws ConfigurationException
    {
        this( new DefaultConfigurationRegistry( data ) );
    }

    @Override
    public void loadConfiguration( final InputStream stream )
        throws ConfigurationException
    {
        loadConfiguration( stream, loadStandardConfigProperties() );
    }

    @Override
    public void loadConfiguration( final InputStream stream, final Properties properties )
            throws ConfigurationException
    {
        final StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource( new PropertiesBasedValueSource( properties ) );

        loadConfiguration( stream, interpolator );
    }

    @Override
    public void loadConfiguration( final InputStream stream, final Interpolator interpolator )
            throws ConfigurationException
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.trace( "Configuration parse starting." );

        List<String> lines;
        try
        {
            lines = readLines( stream );
        }
        catch ( final IOException e )
        {
            throw new ConfigurationException( "Failed to read configuration. Error: %s", e, e.getMessage() );
        }

        String sectionName = ConfigurationSectionListener.DEFAULT_SECTION;
        boolean processSection = dispatch.sectionStarted( sectionName );

        String continuedKey = null;
        StringBuilder continuedVal = null;
        for ( final String line : lines )
        {
            final String trimmed = line.trim();
            if ( trimmed.startsWith( "#" ) )
            {
                continue;
            }

            if ( trimmed.startsWith( "[" ) && trimmed.endsWith( "]" ) )
            {
                if ( trimmed.length() == 2 )
                {
                    continue;
                }

                logger.trace( "Marking section '{}' completed.", sectionName );
                dispatch.sectionComplete( sectionName );
                sectionName = trimmed.substring( 1, trimmed.length() - 1 );

                logger.trace( "Starting section '{}'.", sectionName );
                processSection = dispatch.sectionStarted( sectionName );
            }
            else if ( processSection )
            {
                if ( continuedKey != null )
                {
                    if ( trimmed.endsWith( "\\" ) )
                    {
                        continuedVal.append( trimmed.substring( 0, trimmed.length() - 1 ) );
                    }
                    else
                    {
                        continuedVal.append( trimmed );

                        try
                        {
                            final String value = interpolator.interpolate( continuedVal.toString() );

                            logger.trace( "Section: {}, parameter: {}, value: {} (raw: {})", sectionName, continuedKey,
                                          value.trim(), continuedVal );

                            dispatch.parameter( sectionName, continuedKey.trim(), value.trim() );
                            continuedKey = null;
                            continuedVal = null;
                        }
                        catch ( final InterpolationException e )
                        {
                            throw new ConfigurationException( "Failed to resolve expressions in configuration '%s' (raw value: '%s'). Reason: %s", e,
                                                              continuedKey, continuedVal, e.getMessage() );
                        }
                    }
                }
                else
                {
                    final Matcher matcher = parameter.matcher( line );
                    if ( matcher.matches() )
                    {
                        final String key = matcher.group( 1 );
                        String value = matcher.group( 2 )
                                              .trim();

                        if ( value.endsWith( "\\" ) )
                        {
                            continuedKey = key;
                            continuedVal = new StringBuilder( value.substring( 0, value.length() - 1 ) );
                            continue;
                        }

                        String rawVal = value;
                        try
                        {
                            value = interpolator.interpolate( rawVal );
                        }
                        catch ( final InterpolationException e )
                        {
                            throw new ConfigurationException( "Failed to resolve expressions in configuration '%s' (raw value: '%s'). Reason: %s", e,
                                                              key, value, e.getMessage() );
                        }

                        logger.trace( "Section: {}, parameter: {}, value: {} (raw: {})", sectionName, key.trim(),
                                      value.trim(), rawVal );

                        dispatch.parameter( sectionName, key.trim(), value.trim() );
                    }
                }
            }
        }

        logger.trace( "Marking section '{}' completed.", sectionName );
        dispatch.sectionComplete( sectionName );

        logger.trace( "Configuration parse complete." );
        dispatch.configurationParsed();
    }

}
