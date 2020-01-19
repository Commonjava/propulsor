package org.commonjava.propulsor.metrics;

import org.apache.commons.lang3.ClassUtils;

import static com.codahale.metrics.MetricRegistry.name;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.commonjava.propulsor.metrics.MetricsConstants.DEFAULT;

public class MetricsUtils
{
    /**
     * Get default metric name. Use abbreviated package name, e.g., foo.bar.ClassA.methodB -> f.b.ClassA.methodB
     */
    public static String getDefaultName( Class<?> declaringClass, String method )
    {
        // minimum len 1 shortens the package name and keeps class name
        String cls = ClassUtils.getAbbreviatedName( declaringClass.getName(), 1 );
        return name( cls, method );
    }

    /**
     * Get default metric name. Use abbreviated package name, e.g., foo.bar.ClassA.methodB -> f.b.ClassA.methodB
     */
    public static String getDefaultName( String declaringClass, String method )
    {
        // minimum len 1 shortens the package name and keeps class name
        String cls = ClassUtils.getAbbreviatedName( declaringClass, 1 );
        return name( cls, method );
    }

    /**
     * Get the metric fullname with no default value.
     * @param nameParts user specified name parts
     */
    public static String getSupername( String nodePrefix, String... nameParts )
    {
        return name( nodePrefix, nameParts );
    }

    /**
     * Get the metric fullname.
     * @param name user specified name
     * @param defaultName 'class name + method name', not null.
     */
    public static String getName( String nodePrefix, String name, String defaultName, String... suffix )
    {
        if ( isBlank( name ) || name.equals( DEFAULT ) )
        {
            name = defaultName;
        }

        return name( name( nodePrefix, name ), suffix );
    }
}
