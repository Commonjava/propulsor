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

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClientHttpResponseErrorDetails
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final int code;

    private final String reason;

    private final ProtocolVersion version;

    private final String body;

    public ClientHttpResponseErrorDetails( final HttpResponse response )
    {
        final StatusLine sl = response.getStatusLine();
        this.code = sl.getStatusCode();
        this.reason = sl.getReasonPhrase();
        this.version = sl.getProtocolVersion();

        String body = null;
        if ( response.getEntity() != null )
        {
            try
            {
                body = EntityUtils.toString( response.getEntity() );
            }
            catch ( final ParseException e )
            {
                logger.debug( "Failed to retrieve error response body.", e );
            }
            catch ( final IOException e )
            {
                logger.debug( "Failed to retrieve error response body.", e );
            }
        }

        this.body = body;
    }

    @Override
    public String toString()
    {
        String bodyStr = "";
        if ( body != null && body.length() > 0 )
        {
            bodyStr = String.format(
                    "\nBody:\n\n=========================================\n%s\n\n=========================================",
                    body );
        }

        return String.format( "Status: %d %s (%s)%s",
                              code, reason, version, bodyStr );
    }

}
