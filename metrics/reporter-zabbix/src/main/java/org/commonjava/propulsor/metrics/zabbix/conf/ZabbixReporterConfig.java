package org.commonjava.propulsor.metrics.zabbix.conf;

import org.commonjava.propulsor.metrics.spi.ReporterConfigurator;
import org.commonjava.util.jhttpc.model.SiteConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

/**
 * Created by jdcasey on 4/4/18.
 */
@ApplicationScoped
public class ZabbixReporterConfig
        extends ReporterConfigurator<ZabbixReporterConfig>
{
    private transient SiteConfig httpSiteConfig;

    private String httpHost;

    private int jsonRpcPort;

    private String httpUri;

    private int jsonRpcSocketTimeout;

    private int jsonRpcConnectionTimeout;

    private TimeUnit durationUnit;

    private TimeUnit rateUnit;

    @Override
    public ZabbixReporterConfig getConfiguration()
    {
        return this;
    }

    @Override
    protected void handleParam( final String name, final String value )
    {
    }

    public SiteConfig getHttpSiteConfig()
    {
        return httpSiteConfig;
    }

    public String getHttpUri()
    {
        return httpUri;
    }

    public String getHost()
    {
        return httpHost;
    }

    public int getJsonRpcPort()
    {
        return jsonRpcPort;
    }

    public void setJsonRpcPort( final int jsonRpcPort )
    {
        this.jsonRpcPort = jsonRpcPort;
    }

    public int getJsonRpcSocketTimeout()
    {
        return jsonRpcSocketTimeout;
    }

    public int getJsonRpcConnectionTimeout()
    {
        return jsonRpcConnectionTimeout;
    }

    public TimeUnit getDurationUnit()
    {
        return durationUnit;
    }

    public TimeUnit getRateUnit()
    {
        return rateUnit;
    }
}
