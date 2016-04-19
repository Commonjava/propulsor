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

import java.io.NotSerializableException;
import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Signals an error communicating with the server.
 * @author jdcasey
 */
public class ClientHttpException
    extends Exception
{
    private Object[] params;

    private transient String formattedMessage;

    private final int statusCode;

    public ClientHttpException( final int statusCode, final String message, final Object... params )
    {
        super( message );
        this.statusCode = statusCode;
        this.params = params;
    }

    public ClientHttpException( final int statusCode, final String message, final Throwable cause,
                                 final Object... params )
    {
        super( message, cause );
        this.statusCode = statusCode;
        this.params = params;
    }

    public ClientHttpException( final String message, final Object... params )
    {
        super( message );
        this.params = params;
        this.statusCode = -1;
    }

    public ClientHttpException( final String message, final Throwable cause, final Object... params )
    {
        super( message, cause );
        this.params = params;
        this.statusCode = -1;
    }

    private static final long serialVersionUID = 1L;

    @Override
    public synchronized String getMessage()
    {
        if ( formattedMessage == null )
        {
            final String format = super.getMessage();
            if ( params == null || params.length < 1 )
            {
                formattedMessage = format;
            }
            else
            {
                final String original = formattedMessage;
                try
                {
                    formattedMessage = String.format( format.replaceAll( "\\{\\}", "%s" ), params );
                }
                catch ( final Error e )
                {
                }
                catch ( final RuntimeException e )
                {
                }
                catch ( final Exception e )
                {
                }

                if ( formattedMessage == null || original == formattedMessage )
                {
                    try
                    {
                        formattedMessage = MessageFormat.format( format, params );
                    }
                    catch ( final Error e )
                    {
                        formattedMessage = format;
                        throw e;
                    }
                    catch ( final RuntimeException e )
                    {
                        formattedMessage = format;
                        throw e;
                    }
                    catch ( final Exception e )
                    {
                        formattedMessage = format;
                    }
                }
            }
        }

        return formattedMessage;
    }

    /**
     * Stringify all parameters pre-emptively on serialization, to prevent {@link NotSerializableException}.
     * Since all parameters are used in {@link String#format} or {@link MessageFormat#format}, flattening them
     * to strings is an acceptable way to provide this functionality without making the use of {@link Serializable}
     * viral.
     */
    private Object writeReplace()
    {
        final Object[] newParams = new Object[params.length];
        int i = 0;
        for ( final Object object : params )
        {
            newParams[i] = String.valueOf( object );
            i++;
        }

        this.params = newParams;
        return this;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

}
