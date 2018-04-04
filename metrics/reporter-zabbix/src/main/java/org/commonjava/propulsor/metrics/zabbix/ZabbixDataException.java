package org.commonjava.propulsor.metrics.zabbix;

import org.commonjava.propulsor.metrics.ManagedMetricsException;

/**
 * Created by jdcasey on 4/4/18.
 */
public class ZabbixDataException
        extends ManagedMetricsException
{
    public ZabbixDataException( final String format, final Throwable cause, final Object... params )
    {
        super( format, cause, params );
    }

    public ZabbixDataException( final String format, final Object... params )
    {
        super( format, params );
    }
}
