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

import org.commonjava.propulsor.metrics.ManagedMetricsException;
import org.commonjava.propulsor.metrics.zabbix.ZabbixConfigurationException;
import org.commonjava.propulsor.metrics.zabbix.ZabbixDataException;
import org.commonjava.propulsor.metrics.zabbix.http.ZabbixHttpClient;
import org.commonjava.propulsor.metrics.zabbix.cache.ZabbixStructuresCache;
import org.commonjava.propulsor.metrics.zabbix.conf.ZabbixReporterConfig;
import org.commonjava.util.jhttpc.HttpFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiabai on 4/1/17.
 */
public class ZabbixJsonRpcClient
{
    private static final Logger logger = LoggerFactory.getLogger( ZabbixJsonRpcClient.class );

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
        this.sender = new ZabbixSocketHandler( config.getHost(), config.getJsonRpcPort() );
    }

    String checkHostGroup( String hostGroup ) throws IOException, ManagedMetricsException
    {
        if ( structuresCache.getHostGroup( hostGroup ) == null )
        {
            try
            {
                this.zabbixApiInit();
                String groupid = zabbixApi.getHostgroup( hostGroup );
                if ( groupid == null )
                {
                    groupid = zabbixApi.hostgroupCreate( hostGroup );
                    structuresCache.putHostGroup( hostGroup, groupid );
                }
                structuresCache.putHostGroup( hostGroup, groupid );
                return groupid;
            }
            finally
            {
                this.destroy();
            }
        }
        return null;
    }

    String checkHost( String host, String ip ) throws IOException, ManagedMetricsException
    {
        try
        {
            if ( structuresCache.getHost( host ) == null )
            {
                this.zabbixApiInit();
                String hostid = zabbixApi.getHost( host );
                if ( hostid != null )
                {
                    structuresCache.putHost( host, hostid );

                }
                else
                {// host not exists, create it.

                    hostid = zabbixApi.hostCreate( host, structuresCache.getHostGroup( hostGroup ), ip );
                    structuresCache.putHost( host, hostid );
                }
                return hostid;
            }
        }
        finally
        {
            this.destroy();
        }
        return null;
    }

    private String itemCacheKey( String host, String item )
    {
        return host + ":" + item;
    }

    String checkItem( String host, String item, int valueType ) throws IOException,
                                                                       ManagedMetricsException
    {

        try
        {
            if ( structuresCache.getItem( itemCacheKey( host, item ) ) == null )
            {
                this.zabbixApiInit();

                String itemid = zabbixApi.getItem( host, item, structuresCache.getHost( host ) );
                if ( itemid == null )
                {
                    itemid = zabbixApi.createItem( host, item, structuresCache.getHost( host ), valueType );
                    structuresCache.putItem( itemCacheKey( host, item ), itemid );
                }
                else
                {
                    // put into metricsZabbixCache
                    structuresCache.putItem( itemCacheKey( host, item ), itemid );
                }
                return itemid;
            }
        }
        finally
        {
            this.destroy();
        }

        return null;
    }

    public SocketResult send( DataObject dataObject ) throws IOException, ZabbixDataException
    {
        return this.send( dataObject, System.currentTimeMillis() / 1000L );
    }

    public SocketResult send( DataObject dataObject, long clock ) throws IOException, ZabbixDataException
    {
        return this.send( Collections.singletonList( dataObject ), clock );
    }

    public SocketResult send( List<DataObject> dataObjectList ) throws IOException, ZabbixDataException
    {
        return this.send( dataObjectList, System.currentTimeMillis() / 1000L );
    }

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
        if ( bCreateNotExistHostGroup )
        {
            try
            {
                checkHostGroup( hostGroup );
            }
            catch ( IndyHttpException e )
            {
                logger.error( "Check HostGroup of Zabbix is error:" + e.getMessage() );
                throw e;
            }
        }
        if ( bCreateNotExistHost )
        {
            try
            {
                checkHost( hostName, ip );
            }
            catch ( IndyHttpException e )
            {
                logger.error( "Check Host of Zabbix is error:" + e.getMessage() );
                throw e;
            }
        }

        if ( bCreateNotExistItem )
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
                try
                {
                    checkItem( hostName, key, vauleType );
                }
                catch ( IndyHttpException e )
                {
                    logger.error( "Check Item of Zabbix is error:" + e.getMessage() );
                    throw e;
                }
            }
        }

        try
        {
            SocketResult senderResult = sender.send( dataObjectList, clock );
            if ( !senderResult.success() )
            {
                logger.error( "send data to zabbix server error! senderResult:" + senderResult );
            }
            return senderResult;
        }
        catch ( IOException e )
        {
            logger.error( "send data to zabbix server error!", e );
            throw e;
        }
    }

    public void destroy()
    {
        if ( bCreateNotExistZabbixApi )
        {
            return;
        }
        if ( zabbixApi != null )
            zabbixApi.destroy();
    }

    private void zabbixApiInit() throws IndyMetricsException, IOException, IndyHttpException
    {
        if ( !bCreateNotExistZabbixApi )
        {
            return;
        }
        if ( this.zabbixHostUrl == null || "".equals( this.zabbixHostUrl ) )
        {
            throw new IndyMetricsException( "can not find Zabbix's Host" );
        }

        zabbixApi = new IndyZabbixApi( this.zabbixHostUrl, indyHttpProvider.createClient( new URL( zabbixHostUrl ).getHost() ) );

        zabbixApi.init();

        if ( this.zabbixUserName == null || "".equals( this.zabbixUserName ) || this.zabbixUserPwd == null || "".equals(
                        this.zabbixUserPwd ) )
        {
            throw new IndyMetricsException( "can not find Zabbix's username or password" );
        }
        boolean login = zabbixApi.login( this.zabbixUserName, this.zabbixUserPwd );

        logger.info( "User:" + this.zabbixUserName + " login is " + login );
    }

}
