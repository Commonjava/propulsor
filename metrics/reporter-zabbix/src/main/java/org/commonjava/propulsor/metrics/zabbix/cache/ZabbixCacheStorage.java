package org.commonjava.propulsor.metrics.zabbix.cache;

import org.commonjava.propulsor.metrics.zabbix.ZabbixConfigurationException;
import org.commonjava.propulsor.metrics.zabbix.ZabbixDataException;

/**
 * Interface to manage storage and retrieval of {@link ZabbixStructuresCache} for local caching of Zabbix configuration.
 */
public interface ZabbixCacheStorage
{
    ZabbixStructuresCache load()
            throws ZabbixConfigurationException, ZabbixDataException;

    void store( ZabbixStructuresCache cache )
            throws ZabbixConfigurationException, ZabbixDataException;
}
