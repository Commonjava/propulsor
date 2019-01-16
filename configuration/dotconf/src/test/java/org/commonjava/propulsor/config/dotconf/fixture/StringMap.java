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
package org.commonjava.propulsor.config.dotconf.fixture;

import java.util.HashMap;

public class StringMap
    extends HashMap<String, String>
{
    private static final long serialVersionUID = 1L;

    public StringMap( final String... parts )
    {
        if ( parts.length % 2 != 0 )
        {
            throw new IllegalArgumentException( "Must have an even number of arguments to form key = value pairs!" );
        }

        String last = null;
        for ( final String part : parts )
        {
            if ( last == null )
            {
                last = part;
            }
            else
            {
                put( last, part );
                last = null;
            }
        }
    }
}
