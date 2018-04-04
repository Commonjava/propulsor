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
package org.commonjava.propulsor.config.section;

import java.util.LinkedHashMap;
import java.util.Map;

import org.commonjava.propulsor.config.ConfigurationException;

public class MapSectionListener
    implements ConfigurationSectionListener<Map<String, String>>
{

    private Map<String, String> parameters;

    @Override
    public void sectionStarted( final String name )
        throws ConfigurationException
    {
        parameters = new LinkedHashMap<String, String>();
    }

    @Override
    public void parameter( final String name, final String value )
        throws ConfigurationException
    {
        parameters.put( name, value );
    }

    @Override
    public void sectionComplete( final String name )
        throws ConfigurationException
    {
        // NOP.
    }

    @Override
    public Map<String, String> getConfiguration()
    {
        return parameters;
    }

}
