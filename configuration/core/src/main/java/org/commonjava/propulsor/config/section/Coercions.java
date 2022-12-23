/**
 * Copyright (C) 2014-2022 Red Hat, Inc. (http://github.com/Commonjava/commonjava)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class Coercions
{
    private static final Map<Class<?>, Function<String, Object>> typeCoercions = new HashMap<>();

    static
    {
        typeCoercions.put( String.class, s -> s );

        typeCoercions.put( Integer.TYPE, s-> Integer.valueOf( s ) );
        typeCoercions.put( Integer.class, s -> Integer.valueOf( s ) );

        typeCoercions.put( Long.TYPE, s -> Long.valueOf( s ) );
        typeCoercions.put( Long.class, s -> Long.valueOf( s ) );

        typeCoercions.put( Short.TYPE, s -> Short.valueOf( s ) );
        typeCoercions.put( Short.class, s -> Short.valueOf( s ) );

        typeCoercions.put( Float.TYPE, s -> Float.valueOf( s ) );
        typeCoercions.put( Float.class, s -> Float.valueOf( s ) );

        typeCoercions.put( Double.TYPE, s -> Double.valueOf( s ) );
        typeCoercions.put( Double.class, s -> Double.valueOf( s ) );

        typeCoercions.put( File.class, s -> new File( s ) );

        typeCoercions.put( Boolean.TYPE, s -> Boolean.valueOf( s ) );
        typeCoercions.put( Boolean.class, s -> Boolean.valueOf( s ) );
    }

    private Coercions()
    {
    }

    public static Object coerce( String param, Class<?> ptype, Object source ) throws ConfigurationException
    {
        Logger logger = LoggerFactory.getLogger( Coercions.class );
        logger.debug( "Retrieving coercion for: {}", ptype );

        Function<String, Object> func = typeCoercions.get( ptype );
        if ( func != null )
        {
            return func.apply( param );
        }

        throw new ConfigurationException(
                        String.format( "Cannot convert String to %s for %s", ptype.getName(), source ) );
    }
}
