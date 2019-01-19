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
package org.commonjava.propulsor.config.section;

import org.commonjava.propulsor.config.ConfigurationException;

/**
 * Created by jdcasey on 3/8/16.
 */
public class SectionListenerWrapper<T>
    implements ConfigurationSectionListener<T>
{
    private ConfigurationSectionListener<T> delegate;

    public SectionListenerWrapper(ConfigurationSectionListener<T> delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void sectionStarted( String name )
            throws ConfigurationException
    {
        beforeSectionStarted( name );
        delegate.sectionStarted( name );
        afterSectionStarted( name );
    }

    protected void beforeSectionStarted( String name )
    {
    }

    protected void afterSectionStarted( String name )
    {
    }

    @Override
    public void parameter( String name, String value )
            throws ConfigurationException
    {
        beforeParameter( name, value );
        delegate.parameter( name, value );
        afterParameter( name, value );
    }

    protected void afterParameter( String name, String value )
    {
    }

    protected void beforeParameter( String name, String value )
    {
    }

    @Override
    public void sectionComplete( String name )
            throws ConfigurationException
    {
        beforeSectionComplete( name );
        delegate.sectionComplete( name );
        afterSectionComplete( name );
    }

    protected void afterSectionComplete( String name )
    {
    }

    protected void beforeSectionComplete( String name )
    {
    }

    @Override
    public T getConfiguration()
    {
        beforeGetConfiguration();
        T config = delegate.getConfiguration();
        afterGetConfiguration( config );

        return config;
    }

    protected void afterGetConfiguration( T config )
    {
    }

    protected void beforeGetConfiguration()
    {
    }
}
