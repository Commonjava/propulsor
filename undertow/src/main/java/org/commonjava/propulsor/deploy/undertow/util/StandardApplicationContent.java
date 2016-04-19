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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class StandardApplicationContent
{

    public static final String application_json = "application/json";

    public static final String application_javascript = "application/javascript";

    public static final String application_xml = "application/xml";

    public static final String application_zip = "application/zip";

    public static final String text_css = "text/css";

    public static final String text_html = "text/html";

    public static final String text_plain = "text/plain";

    private StandardApplicationContent()
    {
    }

    private static final Map<String, String> STANDARD_ACCEPTS =
        Collections.unmodifiableMap( new HashMap<String, String>()
        {
            {
                put( application_json, application_json );
                put( text_html, text_html );
                put( text_plain, text_plain );
                put( application_zip, application_zip );
                put( application_xml, application_xml );
            }

            private static final long serialVersionUID = 1L;
        } );

    public static String getStandardAccept( final String indyAccept )
    {
        return indyAccept == null ? null : STANDARD_ACCEPTS.get( indyAccept );
    }
}
