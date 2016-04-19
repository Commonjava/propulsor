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
package org.commonjava.propulsor.client.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.commonjava.propulsor.client.http.helper.HttpResources;
import org.commonjava.util.jhttpc.HttpFactory;
import org.commonjava.util.jhttpc.JHttpCException;
import org.commonjava.util.jhttpc.auth.ClientAuthenticator;
import org.commonjava.util.jhttpc.auth.PasswordManager;
import org.commonjava.util.jhttpc.model.SiteConfig;
import org.commonjava.util.jhttpc.model.SiteConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.commonjava.propulsor.client.http.helper.HttpResources.cleanupResources;
import static org.commonjava.propulsor.client.http.helper.HttpResources.entityToString;
import static org.commonjava.propulsor.client.http.helper.UrlUtils.buildUrl;

public class ClientHttpSupport
        implements AutoCloseable
{
    private static final int GLOBAL_MAX_CONNECTIONS = 20;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final String baseUrl;

    private final ObjectMapper objectMapper;

    private final HttpFactory httpFactory;

    private final SiteConfig siteConfig;

    public ClientHttpSupport( final String baseUrl, final PasswordManager passwordManager,
                              final ObjectMapper mapper )
            throws ClientHttpException
    {
        this.baseUrl = baseUrl;
        this.objectMapper = mapper;
        this.siteConfig = new SiteConfigBuilder("server", baseUrl).build();
        this.httpFactory = new HttpFactory( passwordManager );
    }

    public ClientHttpSupport( final String baseUrl, final ClientAuthenticator authenticator,
                              final ObjectMapper mapper )
            throws ClientHttpException
    {
        this.baseUrl = baseUrl;
        this.objectMapper = mapper;
        this.siteConfig = new SiteConfigBuilder("server", baseUrl).build();
        this.httpFactory = new HttpFactory( authenticator );
    }

    public ClientHttpSupport( final String baseUrl,
                              final ObjectMapper mapper, final HttpFactory httpFactory )
            throws ClientHttpException
    {
        this.baseUrl = baseUrl;
        this.objectMapper = mapper;
        this.siteConfig = new SiteConfigBuilder("server", baseUrl).build();
        this.httpFactory = httpFactory;
    }

    public void connect()
        throws ClientHttpException
    {
        // NOP, mainly provided for extension.
    }

    public Map<String, String> head( final String path )
            throws ClientHttpException
    {
        return head( path, HttpStatus.SC_OK );
    }

    public Map<String, String> head( final String path, final int... responseCodes )
            throws ClientHttpException
    {
        connect();

        HttpHead request = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;

        try
        {
            request = newJsonHead( buildUrl( baseUrl, path ) );
            client = newClient();
            response = client.execute( request );

            final StatusLine sl = response.getStatusLine();
            if ( !validResponseCode( sl.getStatusCode(), responseCodes ) )
            {
                if ( sl.getStatusCode() == HttpStatus.SC_NOT_FOUND )
                {
                    return null;
                }

                throw new ClientHttpException( sl.getStatusCode(), "Error executing HEAD: %s. Status was: %d %s (%s)",
                                                path, sl.getStatusCode(), sl.getReasonPhrase(),
                                                sl.getProtocolVersion() );
            }

            final Map<String, String> headers = new HashMap<>();
            for ( final Header header : response.getAllHeaders() )
            {
                final String name = header.getName().toLowerCase();

                if ( !headers.containsKey( name ) )
                {
                    headers.put( name, header.getValue() );
                }
            }

            return headers;
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            cleanupResources( request, response, client );
        }
    }

    public <T> T get( final String path, final Class<T> type )
            throws ClientHttpException
    {
        connect();

        HttpGet request = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try
        {
            client = newClient();
            request = newJsonGet( buildUrl( baseUrl, path ) );
            response = client.execute( request );

            final StatusLine sl = response.getStatusLine();

            if ( sl.getStatusCode() != 200 )
            {
                if ( sl.getStatusCode() == 404 )
                {
                    return null;
                }

                throw new ClientHttpException( sl.getStatusCode(), "Error retrieving %s from: %s.\n%s",
                                                type.getSimpleName(), path, new ClientHttpResponseErrorDetails( response ) );
            }

            final String json = entityToString( response );
            logger.debug( "Got JSON:\n\n{}\n\n", json );
            final T value = objectMapper.readValue( json, type );

            logger.debug( "Got result object: {}", value );

            return value;
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            cleanupResources( request, response, client );
        }
    }

    public <T> T get( final String path, final TypeReference<T> typeRef )
            throws ClientHttpException
    {
        connect();

        HttpGet request = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try
        {
            client = newClient();
            request = newJsonGet( buildUrl( baseUrl, path ) );
            response = client.execute( request );
            final StatusLine sl = response.getStatusLine();
            if ( sl.getStatusCode() != 200 )
            {
                if ( sl.getStatusCode() == 404 )
                {
                    return null;
                }

                throw new ClientHttpException( sl.getStatusCode(), "Error retrieving %s from: %s.\n%s",
                                                typeRef.getType(), path, new ClientHttpResponseErrorDetails( response ) );
            }

            final String json = entityToString( response );
            final T value = objectMapper.readValue( json, typeRef );

            return value;
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            cleanupResources( request, response, client );
        }
    }

    public HttpResources getRaw( final HttpGet req )
            throws ClientHttpException
    {
        connect();

        CloseableHttpResponse response = null;
        try
        {
            final CloseableHttpClient client = newClient();

            response = client.execute( req );
            return new HttpResources( req, response, client );
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            // DO NOT CLOSE!!!! We're handing off control of the response to the caller!
            //            closeQuietly( response );
        }
    }

    public HttpResources getRaw( final String path )
            throws ClientHttpException
    {
        return getRaw( path, Collections.singletonMap( "Accept", "*" ) );
    }

    public HttpResources getRaw( final String path, final Map<String, String> headers )
            throws ClientHttpException
    {
        connect();

        CloseableHttpResponse response = null;
        try
        {
            final HttpGet req = newRawGet( buildUrl( baseUrl, path ) );
            final CloseableHttpClient client = newClient();

            response = client.execute( req );
            return new HttpResources( req, response, client );
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            // DO NOT CLOSE!!!! We're handing off control of the response to the caller!
            //            closeQuietly( response );
        }
    }

    public void putWithStream( final String path, final InputStream stream )
            throws ClientHttpException
    {
        putWithStream( path, stream, HttpStatus.SC_CREATED );
    }

    public void putWithStream( final String path, final InputStream stream, final int... responseCodes )
            throws ClientHttpException
    {
        connect();

        final HttpPut put = newRawPut( buildUrl( baseUrl, path ) );
        final CloseableHttpClient client = newClient();
        CloseableHttpResponse response = null;
        try
        {
            put.setEntity( new InputStreamEntity( stream ) );

            response = client.execute( put );
            final StatusLine sl = response.getStatusLine();
            if ( !validResponseCode( sl.getStatusCode(), responseCodes ) )
            {
                throw new ClientProtocolException(
                        new ClientHttpException( sl.getStatusCode(), "Error in response from: %s.\n%s", path,
                                                  new ClientHttpResponseErrorDetails( response ) ) );
            }

        }
        catch ( final ClientProtocolException e )
        {
            final Throwable cause = e.getCause();
            if ( cause != null && ( cause instanceof ClientHttpException ) )
            {
                throw (ClientHttpException) cause;
            }

            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            cleanupResources( put, response, client );
        }
    }

    public boolean put( final String path, final Object value )
            throws ClientHttpException
    {
        return put( path, value, HttpStatus.SC_OK, HttpStatus.SC_CREATED );
    }

    public boolean put( final String path, final Object value, final int... responseCodes )
            throws ClientHttpException
    {
        checkRequestValue( value );

        connect();

        HttpPut put = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try
        {
            client = newClient();
            put = newJsonPut( buildUrl( baseUrl, path ) );

            put.setEntity( new StringEntity( objectMapper.writeValueAsString( value ) ) );

            response = client.execute( put );
            final StatusLine sl = response.getStatusLine();
            if ( !validResponseCode( sl.getStatusCode(), responseCodes ) )
            {
                throw new ClientHttpException( sl.getStatusCode(), "Error in response from: %s.\n%s", path,
                                                new ClientHttpResponseErrorDetails( response ) );
            }
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            cleanupResources( put, response, client );
        }

        return true;
    }

    public HttpResources execute( HttpRequestBase request )
            throws ClientHttpException
    {
        connect();

        CloseableHttpResponse response = null;
        try
        {
            final CloseableHttpClient client = newClient();

            response = client.execute( request );
            return new HttpResources( request, response, client );
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            // DO NOT CLOSE!!!! We're handing off control of the response to the caller!
            //            closeQuietly( response );
        }
    }

    public HttpResources postRaw( final String path, Object value )
            throws ClientHttpException
    {
        return postRaw( path, value, Collections.singletonMap( "Accept", "*" ) );
    }

    public HttpResources postRaw( final String path, Object value, final Map<String, String> headers )
            throws ClientHttpException
    {
        checkRequestValue( value );
        connect();

        CloseableHttpResponse response = null;
        try
        {
            final HttpPost req = newRawPost( buildUrl( baseUrl, path ) );
            if ( headers != null )
            {
                for ( String key : headers.keySet() )
                {
                    req.setHeader( key, headers.get( key ) );
                }
            }

            req.setEntity( new StringEntity( objectMapper.writeValueAsString( value ) ) );

            final CloseableHttpClient client = newClient();

            response = client.execute( req );
            return new HttpResources( req, response, client );
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            // DO NOT CLOSE!!!! We're handing off control of the response to the caller!
            //            closeQuietly( response );
        }
    }

    private void checkRequestValue( Object value )
            throws ClientHttpException
    {
        if ( value == null )
        {
            throw new ClientHttpException( "Cannot use null request value!" );
        }
    }

    public <T> T postWithResponse( final String path, final Object value, final Class<T> type )
            throws ClientHttpException
    {
        return postWithResponse( path, value, type, HttpStatus.SC_CREATED, HttpStatus.SC_OK );
    }

    public <T> T postWithResponse( final String path, final Object value, final Class<T> type,
                                   final int... responseCodes )
            throws ClientHttpException
    {
        checkRequestValue( value );

        connect();

        HttpPost post = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try
        {
            client = newClient();
            post = newJsonPost( buildUrl( baseUrl, path ) );

            post.setEntity( new StringEntity( objectMapper.writeValueAsString( value ) ) );

            response = client.execute( post );

            final StatusLine sl = response.getStatusLine();
            if ( !validResponseCode( sl.getStatusCode(), responseCodes ) )
            {
                throw new ClientHttpException( sl.getStatusCode(), "Error retrieving %s from: %s.\n%s",
                                                type.getSimpleName(), path, new ClientHttpResponseErrorDetails( response ) );
            }

            final String json = entityToString( response );
            return objectMapper.readValue( json, type );
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            cleanupResources( post, response, client );
        }
    }

    public boolean validResponseCode( final int statusCode, final int[] responseCodes )
    {
        for ( final int code : responseCodes )
        {
            if ( code == statusCode )
            {
                return true;
            }
        }
        return false;
    }

    public <T> T postWithResponse( final String path, final Object value, final TypeReference<T> typeRef )
            throws ClientHttpException
    {
        return postWithResponse( path, value, typeRef, HttpStatus.SC_CREATED );
    }

    public <T> T postWithResponse( final String path, final Object value, final TypeReference<T> typeRef,
                                   final int... responseCodes )
            throws ClientHttpException
    {
        checkRequestValue( value );

        connect();

        HttpPost post = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try
        {
            client = newClient();
            post = newJsonPost( buildUrl( baseUrl, path ) );

            post.setEntity( new StringEntity( objectMapper.writeValueAsString( value ) ) );

            response = client.execute( post );

            final StatusLine sl = response.getStatusLine();
            if ( !validResponseCode( sl.getStatusCode(), responseCodes ) )
            {
                throw new ClientHttpException( sl.getStatusCode(), "Error retrieving %s from: %s.\n%s",
                                                typeRef.getType(), path, new ClientHttpResponseErrorDetails( response ) );
            }

            final String json = entityToString( response );
            return objectMapper.readValue( json, typeRef );
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            cleanupResources( post, response, client );
        }
    }

    @Override
    public void close()
            throws IOException
    {
        logger.debug( "Shutting down indy client HTTP manager" );
        httpFactory.close();
    }

    public void delete( final String path )
            throws ClientHttpException
    {
        delete( path, HttpStatus.SC_NO_CONTENT );
    }

    public void delete( final String path, final int... responseCodes )
            throws ClientHttpException
    {
        connect();

        HttpDelete delete = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try
        {
            client = newClient();
            delete = newDelete( buildUrl( baseUrl, path ) );

            response = client.execute( delete );
            final StatusLine sl = response.getStatusLine();
            if ( !validResponseCode( sl.getStatusCode(), responseCodes ) )
            {
                throw new ClientHttpException( sl.getStatusCode(), "Error deleting: %s.\n%s", path,
                                                new ClientHttpResponseErrorDetails( response ) );
            }
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            cleanupResources( delete, response, client );
        }
    }

    public boolean exists( final String path )
            throws ClientHttpException
    {
        return exists( path, null, HttpStatus.SC_OK );
    }

    public boolean exists( final String path, Supplier<Map<String, String>> querySupplier )
            throws ClientHttpException
    {
        return exists( path, querySupplier, HttpStatus.SC_OK );
    }

    public boolean exists( final String path, final int... responseCodes )
            throws ClientHttpException
    {
        return exists( path, null, responseCodes );
    }

    public boolean exists( final String path, Supplier<Map<String, String>> querySupplier, final int... responseCodes )
            throws ClientHttpException
    {
        connect();

        HttpHead request = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try
        {
            client = newClient();
            request = newJsonHead( buildUrl( baseUrl, querySupplier, path ) );

            response = client.execute( request );
            final StatusLine sl = response.getStatusLine();
            if ( validResponseCode( sl.getStatusCode(), responseCodes ) )
            {
                return true;
            }
            else if ( sl.getStatusCode() == HttpStatus.SC_NOT_FOUND )
            {
                return false;
            }

            throw new ClientHttpException( sl.getStatusCode(), "Error checking existence of: %s.\n%s", path,
                                            new ClientHttpResponseErrorDetails( response ) );
        }
        catch ( final IOException e )
        {
            throw new ClientHttpException( "Indy request failed: %s", e, e.getMessage() );
        }
        finally
        {
            cleanupResources( request, response, client );
        }
    }

    public void cleanup( final HttpRequest request, final HttpResponse response, final CloseableHttpClient client )
    {
        cleanupResources( request, response, client );
    }

    public String toIndyUrl( final String... path )
    {
        return buildUrl( baseUrl, path );
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public CloseableHttpClient newClient()
            throws ClientHttpException
    {
        try
        {
            return httpFactory.createClient( siteConfig );
        }
        catch ( JHttpCException e )
        {
            throw new ClientHttpException( "Failed to create HTTP client: %s", e, e.getMessage() );
        }
    }

    public HttpGet newRawGet( final String url )
    {
        final HttpGet req = new HttpGet( url );
        return req;
    }

    public HttpGet newJsonGet( final String url )
    {
        final HttpGet req = new HttpGet( url );
        addJsonHeaders( req );
        return req;
    }

    public HttpHead newJsonHead( final String url )
    {
        final HttpHead req = new HttpHead( url );
        addJsonHeaders( req );
        return req;
    }

    public void addJsonHeaders( final HttpUriRequest req )
    {
        req.addHeader( "Accept", "application/json" );
        req.addHeader( "Content-Type", "application/json" );
    }

    public HttpDelete newDelete( final String url )
    {
        final HttpDelete req = new HttpDelete( url );
        return req;
    }

    public HttpPut newJsonPut( final String url )
    {
        final HttpPut req = new HttpPut( url );
        addJsonHeaders( req );
        return req;
    }

    public HttpPut newRawPut( final String url )
    {
        final HttpPut req = new HttpPut( url );
        return req;
    }

    public HttpPost newJsonPost( final String url )
    {
        final HttpPost req = new HttpPost( url );
        addJsonHeaders( req );
        return req;
    }

    public HttpPost newRawPost( final String url )
    {
        final HttpPost req = new HttpPost( url );
        req.addHeader( "Content-Type", "application/json" );
        return req;
    }

    public ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

}
