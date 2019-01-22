/**
 * Copyright (C) 2015 John Casey (jdcasey@commonjava.org)
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
package org.commonjava.propulsor.metrics.zabbix.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.commonjava.propulsor.metrics.zabbix.ZabbixConfigurationException;
import org.commonjava.propulsor.metrics.zabbix.conf.ZabbixReporterConfig;
import org.commonjava.util.jhttpc.HttpFactory;
import org.commonjava.util.jhttpc.JHttpCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by xiabai on 3/31/17.
 */
public class ZabbixHttpClient
{
    private static final Logger logger = LoggerFactory.getLogger( ZabbixHttpClient.class );

    private final ZabbixReporterConfig config;

    private final HttpFactory httpFactory;

    private volatile String auth;

    public ZabbixHttpClient( ZabbixReporterConfig config, HttpFactory httpFactory )
    {
        this.config = config;
        this.httpFactory = httpFactory;
    }

    public boolean login( String user, String password )
            throws IOException, ZabbixConfigurationException
    {
        this.auth = null;
        JsonRpcRequest request = JsonRpcRequestBuilder.newBuilder()
                                                      .paramEntry( "user", user )
                                                      .paramEntry( "password", password )
                                                      .method( "user.login" )
                                                      .build();
        JsonNode response = call( request );
        String auth = response.get( "result" ).asText();
        if ( auth != null && !auth.isEmpty() )
        {
            this.auth = auth;
            return true;
        }
        return false;
    }

    public String apiVersion()
            throws IOException, ZabbixConfigurationException
    {
        JsonRpcRequest request = JsonRpcRequestBuilder.newBuilder().method( "apiinfo.version" ).build();
        JsonNode response = call( request );
        return response.get( "result" ).asText();
    }

    /**
     *
     * @param name
     * @return hostid
     */
    public String getHost( String name )
            throws IOException, ZabbixConfigurationException
    {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        ArrayNode arrayNode = mapper.createArrayNode();
        arrayNode.add( name );
        jsonNode.put( "host", arrayNode );
        JsonRpcRequest
                request = JsonRpcRequestBuilder.newBuilder().method( "host.get" ).paramEntry( "filter", jsonNode ).build();
        JsonNode response = call( request );
        if ( response.get( "result" ).isNull() || response.get( "result" ).get( 0 ) == null )
        {
            return null;
        }
        return response.get( "result" ).get( 0 ).get( "hostid" ).asText();
    }

    /**
     *
     * @param host
     * @param groupId
     * @param ip
     * @return hostid
     */
    public String hostCreate( String host, String groupId, String ip )
            throws IOException, ZabbixConfigurationException
    {
        // host not exists, create it.
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode groups = mapper.createArrayNode();
        ObjectNode group = mapper.createObjectNode();
        group.put( "groupid", groupId );
        groups.add( group );

        // "interfaces": [
        // {
        // "type": 1,
        // "main": 1,
        // "useip": 1,
        // "ip": "192.168.3.1",
        // "dns": "",
        // "port": "10050"
        // }
        // ],

        ObjectNode interface1 = mapper.createObjectNode();
        //        JSONObject interface1 = new JSONObject();
        interface1.put( "type", 1 );
        interface1.put( "main", 1 );
        interface1.put( "useip", 1 );
        interface1.put( "ip", ip );
        interface1.put( "dns", "" );
        interface1.put( "port", "10050" );

        JsonRpcRequest request = JsonRpcRequestBuilder.newBuilder()
                                                      .method( "host.create" )
                                                      .paramEntry( "host", host )
                                                      .paramEntry( "groups", groups )
                                                      .paramEntry( "interfaces", new Object[] { interface1 } )
                                                      .build();
        JsonNode response = call( request );
        return response.get( "result" ).get( "hostids" ).get( 0 ).asText();
    }

    /**
     *
     * @param name
     * @return groupId
     */
    public String getHostgroup( String name )
            throws IOException, ZabbixConfigurationException
    {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        ArrayNode arrayNode = mapper.createArrayNode();
        arrayNode.add( name );
        jsonNode.put( "name", arrayNode );
        JsonRpcRequest request =
                        JsonRpcRequestBuilder.newBuilder().method( "hostgroup.get" ).paramEntry( "filter", jsonNode ).build();
        JsonNode response = call( request );
        if ( response.get( "result" ).isNull() || response.get( "result" ).get( 0 ) == null )
        {
            return null;
        }
        return response.get( "result" ).get( 0 ).get( "groupid" ).asText();
    }

    /**
     *
     * @param name
     * @return groupId
     */
    public String hostgroupCreate( String name )
            throws IOException, ZabbixConfigurationException
    {
        JsonRpcRequest
                request = JsonRpcRequestBuilder.newBuilder().method( "hostgroup.create" ).paramEntry( "name", name ).build();
        JsonNode response = call( request );
        return response.get( "result" ).get( "groupids" ).get( 0 ).toString();
    }

    public JsonNode call( JsonRpcRequest request )
            throws IOException, ZabbixConfigurationException
    {
        if ( request.getAuth() == null )
        {
            request.setAuth( this.auth );
        }

        try(CloseableHttpClient client = httpFactory.createClient( config.getHttpSiteConfig() ) )
        {
            ObjectMapper mapper = new ObjectMapper();
            HttpUriRequest httpRequest = org.apache.http.client.methods.RequestBuilder.post()
                                                                                      .setUri( config.getHttpUri() )
                                                                                      .addHeader( "Content-Type",
                                                                                                  "application/json" )
                                                                                      .setEntity( new StringEntity(
                                                                                              mapper.writeValueAsString(
                                                                                                      request ),
                                                                                              ContentType.APPLICATION_JSON ) )
                                                                                      .build();
            CloseableHttpResponse response = client.execute( httpRequest );
            String result = EntityUtils.toString( response.getEntity() );
            logger.info( result );
            return mapper.readTree( result );
        }
        catch ( JHttpCException e )
        {
            throw new ZabbixConfigurationException( "HTTP connection failed: %s", e, e.getMessage() );
        }
    }

    public String createItem( String host, String item, String hostid, int valueType )
            throws IOException, ZabbixConfigurationException
    {
        // create item
        int type = 2; // trapper
        int value_type = valueType; // float
        int delay = 30;
        JsonRpcRequest request = JsonRpcRequestBuilder.newBuilder()
                                                      .method( "item.create" )
                                                      .paramEntry( "name", item )
                                                      .paramEntry( "key_", item )
                                                      .paramEntry( "hostid", hostid )
                                                      .paramEntry( "type", type )
                                                      .paramEntry( "value_type", value_type )
                                                      .paramEntry( "delay", delay )
                                                      .build();

        JsonNode response = call( request );
        return response.get( "result" ).findValues( "itemids" ).get( 0 ).asText();
    }

    public String getItem( String host, String item, String hostid )
            throws IOException, ZabbixConfigurationException
    {
        ObjectMapper mapper = new ObjectMapper();

        ArrayNode groups = mapper.createArrayNode();
        ObjectNode search = mapper.createObjectNode();
        search.put( "key_", item );
        JsonRpcRequest getRequest = JsonRpcRequestBuilder.newBuilder()
                                                         .method( "item.get" )
                                                         .paramEntry( "hostids", hostid )
                                                         .paramEntry( "search", search )
                                                         .build();
        JsonNode response = call( getRequest );
        if ( response.get( "result" ).isNull() || response.get( "result" ).get( 0 ) == null )
        {
            return null;
        }
        return response.get( "result" ).get( 0 ).get( "itemid" ).asText();
    }
}
