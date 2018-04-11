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

import org.commonjava.propulsor.config.annotation.SectionName;
import org.commonjava.propulsor.config.section.ConfigurationSectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigUtils
{

    private ConfigUtils()
    {
    }

    public static String getSectionName( Class<?> cls )
    {
        Logger logger = LoggerFactory.getLogger( ConfigUtils.class );
        SectionName anno = null;
        do
        {
            logger.trace( "Retrieving @SectionName annotation from: {}", cls.getName() );
            anno = cls.getAnnotation( SectionName.class );
            cls = cls.getSuperclass();
        }
        while ( anno == null && cls != null );

        logger.debug( "Anotation: {} (value: {})", anno, ( anno == null ? "NONE" : anno.value() ) );

        return anno == null ? ConfigurationSectionListener.DEFAULT_SECTION : anno.value();
    }

    public static String getSectionName( Object instance )
    {
        Logger logger = LoggerFactory.getLogger( ConfigUtils.class );
        logger.trace( "Retrieving @SectionName annotation from instance: {}", instance );

        return getSectionName( instance.getClass() );
    }
}
