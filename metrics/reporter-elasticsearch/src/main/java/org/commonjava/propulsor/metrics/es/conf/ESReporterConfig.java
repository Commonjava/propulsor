package org.commonjava.propulsor.metrics.es.conf;

import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;
import org.commonjava.propulsor.metrics.spi.ReporterConfigurator;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@SectionName( "metrics.elasticsearch" )
public class ESReporterConfig
                extends ReporterConfigurator<ESReporterConfig>
{
    private static final String DEFAULT_INDEX_DATE_FORMAT = "YYYY-MM-dd";

    private static final Integer DEFAULT_TIMEOUT = Integer.valueOf( 2000 );

    private String indexDateFormat;

    private String indexName;

    private String hosts;

    private Integer timeout;

    public String getIndexDateFormat()
    {
        return indexDateFormat == null ? DEFAULT_INDEX_DATE_FORMAT : indexDateFormat;
    }

    @ConfigName( "index.date.format" )
    public void setIndexDateFormat( String indexDateFormat )
    {
        this.indexDateFormat = indexDateFormat;
    }

    public String getIndexName()
    {
        return indexName;
    }

    @ConfigName( "index.name" )
    public void setIndexName( String indexName )
    {
        this.indexName = indexName;
    }

    public String getHosts()
    {
        return hosts;
    }

    @ConfigName( "hosts" )
    public void setHosts( String hosts )
    {
        this.hosts = hosts;
    }

    public int getTimeout()
    {
        return timeout == null ? DEFAULT_TIMEOUT : timeout;
    }

    @ConfigName( "timeout" )
    public void setTimeout( Integer timeout )
    {
        this.timeout = timeout;
    }
}
