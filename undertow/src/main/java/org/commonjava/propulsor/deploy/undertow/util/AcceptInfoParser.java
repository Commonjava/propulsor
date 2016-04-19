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
package org.commonjava.propulsor.deploy.undertow.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.apache.commons.lang.StringUtils.join;

/**
 * <pre>
 * IN: application/myapp-v1+json
 * OUT: {@link AcceptInfo} with raw equal to that given above, base = application/myapp+json, version=v1
 * </pre>
 */
public class AcceptInfoParser
{

    public static final String DEFAULT_VERSION = "v1";

    private String appId;

    private String defaultVersion;

    public AcceptInfoParser( String appId, String defaultVersion )
    {
        this.appId = appId;
        this.defaultVersion = defaultVersion;
    }

    public AcceptInfoParser( String appId )
    {
        this.appId = appId;
        this.defaultVersion = DEFAULT_VERSION;
    }

    public List<AcceptInfo> parse( final String... accepts )
    {
        return parse( Arrays.asList( accepts ) );
    }

    public List<AcceptInfo> parse( final Collection<String> accepts )
    {
        final Logger logger = LoggerFactory.getLogger( AcceptInfo.class );

        final List<String> raw = new ArrayList<String>();
        for ( final String accept : accepts )
        {
            final String[] parts = accept.split( "\\s*,\\s*" );
            if ( parts.length == 1 )
            {
                logger.info( "adding atomic accept header: '{}'", accept );
                raw.add( accept );
            }
            else
            {
                logger.info( "Adding split header values: '{}'", join( parts, "', '" ) );
                raw.addAll( Arrays.asList( parts ) );
            }
        }

        logger.info( "Got raw ACCEPT header values:\n  {}", join( raw, "\n  " ) );

        if ( raw == null || raw.isEmpty() )
        {
            return Collections.singletonList( new AcceptInfo( AcceptInfo.ACCEPT_ANY, AcceptInfo.ACCEPT_ANY,
                                                              defaultVersion ) );
        }

        final List<AcceptInfo> acceptInfos = new ArrayList<AcceptInfo>();
        for ( final String r : raw )
        {
            String cleaned = r.toLowerCase();
            final int qIdx = cleaned.indexOf( ';' );
            if ( qIdx > -1 )
            {
                // FIXME: We shouldn't discard quality suffix...
                cleaned = cleaned.substring( 0, qIdx );
            }

            logger.info( "Cleaned up: {} to: {}", r, cleaned );

            final String appPrefix = "application/" + appId + "-";

            logger.info( "Checking for ACCEPT header starting with: '{}' and containing: '+' (header value is: '{}')",
                         appPrefix, cleaned );
            if ( cleaned.startsWith( appPrefix ) && cleaned.contains( "+" ) )
            {
                final String[] acceptParts = cleaned.substring( appPrefix.length() )
                                                    .split( "\\+" );

                acceptInfos.add( new AcceptInfo( cleaned, "application/" + acceptParts[1], acceptParts[0] ) );
            }
            else
            {
                acceptInfos.add( new AcceptInfo( cleaned, cleaned, defaultVersion ) );
            }
        }

        return acceptInfos;
    }

    public List<AcceptInfo> parse( final Enumeration<String> accepts )
    {
        return parse( Collections.list( accepts ) );
    }

    public String getDefaultVersion()
    {
        return defaultVersion;
    }

}