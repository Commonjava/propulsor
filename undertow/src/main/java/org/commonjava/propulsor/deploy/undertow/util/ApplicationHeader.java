/**
 * Copyright (C) 2011 Red Hat, Inc. (jdcasey@commonjava.org)
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

public enum ApplicationHeader
{

    content_type( "Content-Type" ),
    location( "Location" ),
    uri( "URI" ),
    content_length( "Content-Length" ),
    last_modified( "Last-Modified" ),
    deprecated( "Deprecated-Use-Alt" ),
    accept( "Accept" );

    private final String key;

    private ApplicationHeader( final String key )
    {
        this.key = key;
    }

    public String key()
    {
        return key;
    }

}
