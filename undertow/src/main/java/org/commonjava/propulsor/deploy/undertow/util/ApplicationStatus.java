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

public enum ApplicationStatus
{

    /* @formatter:off */
    OK( 200, "Ok" ), 
    CREATED( 201, "Created" ), 
    NO_CONTENT(204, "No Content"),
    
    MOVED_PERMANENTLY( 301, "Moved Permanently" ),
    FOUND( 302, "Found" ),
    
    NOT_MODIFIED( 304, "Not Modified" ),
    
    BAD_REQUEST( 400, "Bad Request" ), 
    
    NOT_FOUND( 404, "Not Found" ), 
    
    CONFLICT( 409, "Conflict" ),
    
    SERVER_ERROR( 500, "Internal Server Error" );
    /* @formatter:on */

    private int status;

    private String message;

    private ApplicationStatus( final int status, final String messsage )
    {
        this.status = status;
        this.message = messsage;
    }

    public int code()
    {
        return status;
    }

    public String message()
    {
        return message;
    }

    public static ApplicationStatus getStatus( final int status )
    {
        for ( final ApplicationStatus as : values() )
        {
            if ( as.code() == status )
            {
                return as;
            }
        }

        return null;
    }

}
