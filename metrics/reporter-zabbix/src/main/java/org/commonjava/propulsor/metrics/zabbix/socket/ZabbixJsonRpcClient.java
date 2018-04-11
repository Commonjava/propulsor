/**
 * Copyright (C) 2011-2017 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.propulsor.metrics.zabbix.socket;

import org.commonjava.propulsor.metrics.zabbix.ZabbixConfigurationException;
import org.commonjava.propulsor.metrics.zabbix.ZabbixDataException;
import org.commonjava.propulsor.metrics.zabbix.http.ZabbixHttpClient;
import org.commonjava.propulsor.metrics.zabbix.cache.ZabbixStructuresCache;
import org.commonjava.propulsor.metrics.zabbix.conf.ZabbixReporterConfig;
import org.commonjava.util.jhttpc.HttpFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiabai on 4/1/17.
 * Refactored by jdcasey on 4/7/18.
 *
 * Client API wrapping socket communication to Zabbix server. Uses http client if auto-create option is enabled, to
 * create the appropriate structures for receiving data on the Zabbix side.
 */
@ApplicationScoped
public class ZabbixJsonRpcClient
{
    private final ZabbixReporterConfig config;

    private final ZabbixStructuresCache structuresCache;

    private final ZabbixSocketHandler sender;

    private final ZabbixHttpClient httpClient;

    private static final String regEx = "^-?[0-9]+$";

    private static final Pattern pat = Pattern.compile( regEx );

    @Inject
    public ZabbixJsonRpcClient( ZabbixReporterConfig config, HttpFactory httpFactory, ZabbixStructuresCache structuresCache )
    {
        this.config = config;
        this.structuresCache = structuresCache;
        this.httpClient = new ZabbixHttpClient( config, httpFactory );
        this.sender = new ZabbixSocketHandler( config );
    }

    public void start() throws IOException, ZabbixConfigurationException
    {
        if ( config.isAutoCreate() )
        {
            checkHostGroup();
            checkHost();
        }
    }

    private String checkHostGroup() throws IOException, ZabbixConfigurationException
    {
        String hostGroup = config.getHostGroup();
        String hostGroupId = structuresCache.getHostGroup( hostGroup );
        if ( hostGroupId == null )
        {
            hostGroupId = httpClient.getHostgroup( hostGroup );
            if ( hostGroupId == null )
            {
                hostGroupId = httpClient.hostgroupCreate( hostGroup );
                structuresCache.putHostGroup( hostGroup, hostGroupId );
            }
            structuresCache.putHostGroup( hostGroup, hostGroupId );
            return hostGroupId;
        }

        return hostGroupId;
    }

    private String checkHost() throws IOException, ZabbixConfigurationException
    {
        String host = config.getLocalHostName();
        String hostId = structuresCache.getHost( host );
        if ( hostId == null )
        {
            hostId = httpClient.getHost( host );
            if ( hostId != null )
            {
                structuresCache.putHost( host, hostId );

            }
            else
            {
                hostId = httpClient.hostCreate( host, structuresCache.getHostGroup( config.getHostGroup() ),
                                                config.getLocalIpAddress() );

                structuresCache.putHost( host, hostId );
            }
            return hostId;
        }

        return hostId;
    }

    private String itemCacheKey( String host, String item )
    {
        return host + ":" + item;
    }

    private String checkItem( String item, int valueType ) throws IOException,
                    ZabbixConfigurationException
    {
        String host = config.getLocalHostName();
        String itemId = structuresCache.getItem( itemCacheKey( host, item ) );
        if ( itemId == null )
        {
            itemId = httpClient.getItem( host, item, structuresCache.getHost( host ) );
            if ( itemId == null )
            {
                itemId = httpClient.createItem( host, item, structuresCache.getHost( host ), valueType );
                structuresCache.putItem( itemCacheKey( host, item ), itemId );
            }
            else
            {
                // put into metricsZabbixCache
                structuresCache.putItem( itemCacheKey( host, item ), itemId );
            }
            return itemId;
        }

        return itemId;
    }

//    public SocketResult send( DataObject dataObject )
//                    throws IOException, ZabbixDataException, ZabbixConfigurationException
//    {
//        return this.send( dataObject, System.currentTimeMillis() / 1000L );
//    }

    public SocketResult send( DataObject dataObject, long clock )
                    throws IOException, ZabbixDataException, ZabbixConfigurationException
    {
        return this.send( Collections.singletonList( dataObject ), clock );
    }

//    public SocketResult send( List<DataObject> dataObjectList )
//                    throws IOException, ZabbixDataException, ZabbixConfigurationException
//    {
//        return this.send( dataObjectList, System.currentTimeMillis() / 1000L );
//    }

    /**
     *
     * @param dataObjectList
     * @param clock
     *            TimeUnit is SECONDS.
     * @return
     * @throws IOException
     */
    public SocketResult send( List<DataObject> dataObjectList, long clock )
                    throws IOException, ZabbixConfigurationException, ZabbixDataException
    {
        if ( config.isAutoCreate() )
        {
            for ( DataObject object : dataObjectList )
            {
                String key = object.getKey();
                int vauleType = 0;

                Matcher mat = pat.matcher( object.getValue() );

                if ( !mat.find() )
                {
                    vauleType = 4;
                }

                checkItem( key, vauleType );
            }
        }

        SocketResult result = sender.send( dataObjectList, clock );
        if ( !result.success() )
        {
            throw new ZabbixDataException( "Metrics not delivered successfully. %d/%d not processed.",
                                           result.getFailed(), result.getTotal() );
        }

        return result;
    }

}
