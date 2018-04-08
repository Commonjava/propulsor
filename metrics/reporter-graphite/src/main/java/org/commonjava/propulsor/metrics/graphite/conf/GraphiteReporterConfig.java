package org.commonjava.propulsor.metrics.graphite.conf;

import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;
import org.commonjava.propulsor.metrics.spi.ReporterConfigurator;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@SectionName( "metrics.graphite" )
public class GraphiteReporterConfig
    extends ReporterConfigurator<GraphiteReporterConfig>
{
    private String host;

    private Integer port;

    public String getHost()
    {
        return host;
    }

    @ConfigName( "host" )
    public void setHost( String host )
    {
        this.host = host;
    }

    public Integer getPort()
    {
        return port;
    }

    @ConfigName( "port" )
    public void setPort( Integer port )
    {
        this.port = port;
    }
}
