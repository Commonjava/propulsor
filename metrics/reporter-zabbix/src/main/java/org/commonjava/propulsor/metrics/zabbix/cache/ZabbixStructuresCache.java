package org.commonjava.propulsor.metrics.zabbix.cache;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jdcasey on 4/4/18.
 *
 * Model for storing Zabbix host/host-group/item structures locally.
 */
public class ZabbixStructuresCache
{
    private Map<String, String> hostGroups = new ConcurrentHashMap<>();

    private Map<String, String> hosts = new ConcurrentHashMap<>();

    private Map<String, String> items = new ConcurrentHashMap<>();

    public String getHostGroup( String name )
    {
        return hostGroups.get( name );
    }

    public String getHost( String name )
    {
        return hosts.get( name );
    }

    public String getItem( String name )
    {
        return items.get( name );
    }

    public String putHostGroup( String name, String id )
    {
        return hostGroups.put( name, id );
    }

    public String putHost( String name, String id )
    {
        return hosts.put( name, id );
    }

    public String putItem( String name, String id )
    {
        return items.put( name, id );
    }

    public Map<String, String> getHostGroups()
    {
        return hostGroups;
    }

    public void setHostGroups( final Map<String, String> hostGroups )
    {
        this.hostGroups = hostGroups;
    }

    public Map<String, String> getHosts()
    {
        return hosts;
    }

    public void setHosts( final Map<String, String> hosts )
    {
        this.hosts = hosts;
    }

    public Map<String, String> getItems()
    {
        return items;
    }

    public void setItems( final Map<String, String> items )
    {
        this.items = items;
    }
}
