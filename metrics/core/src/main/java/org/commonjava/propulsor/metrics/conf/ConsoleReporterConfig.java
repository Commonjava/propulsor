/**
 * Copyright (C) 2014-2022 Red Hat, Inc. (http://github.com/Commonjava/commonjava)
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
package org.commonjava.propulsor.metrics.conf;

import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;
import org.commonjava.propulsor.metrics.spi.ReporterConfigurator;

import javax.enterprise.context.ApplicationScoped;
import java.io.PrintStream;
import java.util.Locale;
import java.util.TimeZone;

@ApplicationScoped
@SectionName( "metrics.console" )
public class ConsoleReporterConfig
                extends ReporterConfigurator<ConsoleReporterConfig>
{
    private transient TimeZone timeZone;

    private transient Locale locale;

    private String formatTimezone;

    private String formatLocale;

    public String getFormatTimezone()
    {
        return formatTimezone;
    }

    @ConfigName( "format.timezone" )
    public void setFormatTimezone( String formatTimezone )
    {
        this.formatTimezone = formatTimezone;
    }

    public String getFormatLocale()
    {
        return formatLocale;
    }

    @ConfigName( "format.locale" )
    public void setFormatLocale( String formatLocale )
    {
        this.formatLocale = formatLocale;
    }

    public synchronized TimeZone getTimeZone()
    {
        if ( timeZone == null )
        {
            if ( formatTimezone == null )
            {
                timeZone = TimeZone.getDefault();
            }
            else
            {
                timeZone = TimeZone.getTimeZone( formatTimezone );
            }
        }

        return timeZone;
    }

    public synchronized Locale getLocale()
    {
        if ( locale == null )
        {
            if ( formatLocale == null )
            {
                locale = Locale.getDefault();
            }
            else
            {
                locale = Locale.forLanguageTag( formatLocale );
            }
        }

        return locale;
    }

}
