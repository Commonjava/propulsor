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
