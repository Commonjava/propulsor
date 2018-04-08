package org.commonjava.propulsor.config.section;

import org.commonjava.propulsor.config.ConfigurationException;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class Coercions
{
    private static final Map<Class<?>, Function<String, Object>> typeCoercions = new HashMap<>();

    static
    {
        typeCoercions.put( String.class, s -> s );
        typeCoercions.put( Integer.class, s -> Integer.valueOf( s ) );
        typeCoercions.put( Long.class, s -> Long.valueOf( s ) );
        typeCoercions.put( Short.class, s -> Short.valueOf( s ) );
        typeCoercions.put( Float.class, s -> Float.valueOf( s ) );
        typeCoercions.put( Double.class, s -> Double.valueOf( s ) );
        typeCoercions.put( File.class, s -> new File( s ) );
        typeCoercions.put( Boolean.class, s -> Boolean.valueOf( s ) );
    }

    private Coercions()
    {
    }

    public static Object coerce( String param, Class<?> ptype, Object source ) throws ConfigurationException
    {
        Function<String, Object> func = typeCoercions.get( ptype );
        if ( func != null )
        {
            return func.apply( param );
        }

        throw new ConfigurationException(
                        String.format( "Cannot convert String to %s for %s", ptype.getName(), source ) );
    }
}
