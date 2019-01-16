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
package org.commonjava.propulsor.config;

import java.util.HashMap;
import java.util.Map;

import org.commonjava.propulsor.config.annotation.SectionName;
import org.commonjava.propulsor.config.section.BeanSectionListener;
import org.commonjava.propulsor.config.section.ConfigurationSectionListener;
import org.commonjava.propulsor.config.section.TypedConfigurationSectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigurationListener
    implements ConfigurationListener
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Map<String, ConfigurationSectionListener<?>> sectionListeners = new HashMap<String, ConfigurationSectionListener<?>>();

    public DefaultConfigurationListener()
    {
    }

    public DefaultConfigurationListener( final Class<?>... sectionTypes )
        throws ConfigurationException
    {
        for ( final Class<?> type : sectionTypes )
        {
            processSectionAnnotation( type, null );
        }
    }

    public DefaultConfigurationListener( final ConfigurationSectionListener<?>... sectionTypes )
        throws ConfigurationException
    {
        for ( final ConfigurationSectionListener<?> type : sectionTypes )
        {
            if ( type instanceof TypedConfigurationSectionListener )
            {
                final Class<?> cls = ( (TypedConfigurationSectionListener<?>) type ).getConfigurationType();
                processSectionAnnotation( cls, type );
            }
            else
            {
                throw new ConfigurationException( "Cannot automatically register section listener: {}", type );
            }
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private void processSectionAnnotation( final Class cls, final ConfigurationSectionListener listener )
        throws ConfigurationException
    {
        final String key = ConfigUtils.getSectionName( cls );
        registerListener( key, listener == null ? new BeanSectionListener( cls ) : listener );
    }

    private void registerListener( final String key, @SuppressWarnings( "rawtypes" ) final ConfigurationSectionListener listener )
        throws ConfigurationException
    {
        final ConfigurationSectionListener<?> existing = sectionListeners.get( key );
        if ( existing != null && listener != existing )
        {
            throw new ConfigurationException( "Section collision! More than one ConfigurationParser bound to section: {}\n\t{}\n\t{}", key,
                                              sectionListeners.get( key ), listener );
        }

        this.sectionListeners.put( key, listener );
    }

    @Override
    public Map<String, ConfigurationSectionListener<?>> getSectionListeners()
    {
        return sectionListeners;
    }

    public DefaultConfigurationListener with( final ConfigurationSectionListener<?>... listeners )
        throws ConfigurationException
    {
        for ( final ConfigurationSectionListener<?> listener : listeners )
        {
            processSectionAnnotation( listener.getClass(), listener );
        }

        return this;
    }

    public DefaultConfigurationListener with( final ConfigurationSectionListener<?> listener )
        throws ConfigurationException
    {
        return with( null, listener );
    }

    public DefaultConfigurationListener with( final String sectionName, final ConfigurationSectionListener<?> listener )
        throws ConfigurationException
    {
        final String key = sectionName == null ? ConfigUtils.getSectionName( listener.getClass() ) : sectionName;
        logger.info( "+section (listener): {} ({})", key, listener );
        registerListener( key, listener );
        return this;
    }

    public <T> DefaultConfigurationListener with( final Class<T> beanCls )
        throws ConfigurationException
    {
        return with( null, beanCls );
    }

    public <T> DefaultConfigurationListener with( final String sectionName, final Class<T> beanCls )
        throws ConfigurationException
    {
        final String key = sectionName == null ? ConfigUtils.getSectionName( beanCls ) : sectionName;
        logger.info( "+section (class): {} ({})", key, beanCls.getName() );
        registerListener( key, new BeanSectionListener<T>( beanCls ) );
        return this;
    }

    public <T> DefaultConfigurationListener with( final T bean )
        throws ConfigurationException
    {
        return with( null, bean );
    }

    public <T> DefaultConfigurationListener with( final String sectionName, final T bean )
        throws ConfigurationException
    {
        if( bean instanceof ConfigurationSectionListener )
        {
            with( sectionName, (ConfigurationSectionListener) bean );
        }
        else
        {
            final String key = sectionName == null ? ConfigUtils.getSectionName( bean.getClass() ) : sectionName;
            logger.info( "+section (bean): {} ({})", key, bean );
            registerListener( key, new BeanSectionListener<T>( bean ) );
        }

        return this;
    }

    @Override
    public void configurationComplete()
        throws ConfigurationException
    {
    }

    public <T> T getConfiguration( final String sectionName, final Class<T> type )
    {
        final ConfigurationSectionListener<?> listener = sectionListeners.get( sectionName );
        return listener == null ? null : type.cast( listener.getConfiguration() );
    }

    public <T> T getConfiguration( final Class<T> type )
        throws ConfigurationException
    {
        final SectionName secName = type.getAnnotation( SectionName.class );
        if ( secName == null )
        {
            throw new ConfigurationException( "Cannot find @SectionName annotation for: {}. Cannot lookup configuration section.", type.getName() );
        }

        final ConfigurationSectionListener<?> listener = sectionListeners.get( secName.value() );
        final Object configuration = listener == null ? null : listener.getConfiguration();
        return configuration == null ? null : type.cast( configuration );
    }

}
