package org.commonjava.propulsor.metrics.zabbix.conf;

import org.apache.commons.io.FileUtils;
import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;
import org.commonjava.propulsor.metrics.spi.ReporterConfigurator;
import org.commonjava.util.jhttpc.model.SiteConfig;
import org.commonjava.util.jhttpc.model.SiteConfigBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;

/**
 * Created by jdcasey on 4/4/18.
 */
@ApplicationScoped
@SectionName("metrics.zabbix")
public class ZabbixReporterConfig
                extends ReporterConfigurator<ZabbixReporterConfig>
{
    private static final Boolean DEFAULT_AUTO_CREATE = Boolean.FALSE;

    private transient SiteConfig httpSiteConfig;

    private String zabbixHost;

    private int jsonRpcPort;

    private String httpUri;

    private int jsonRpcSocketTimeout;

    private int jsonRpcConnectionTimeout;

    private String localHostName;

    private String hostGroup;

    private Boolean autoCreate;

    private String localIpAddress;

    private String httpUser;

    private Integer httpRequestTimeoutSeconds;

    private Integer httpConnectionTimeoutSeconds;

    private Integer httpMaxConnections;

    private String httpServerCertPemFile;

    private String httpKeyCertPemFile;

    public synchronized SiteConfig getHttpSiteConfig() throws IOException
    {
        if ( httpSiteConfig == null )
        {
            SiteConfigBuilder b = new SiteConfigBuilder( "zabbix", getHttpUri() );
            b.withUser( getHttpUser() )
             .withKeyCertPem( readHttpKeyCertPem() )
             .withServerCertPem( readServerCertPem() )
             .withRequestTimeoutSeconds( getHttpRequestTimeoutSeconds() )
             .withConnectionPoolTimeoutSeconds( getHttpConnectionTimeoutSeconds() )
             .withMaxConnections( getHttpMaxConnections() )
             .build();
        }

        return httpSiteConfig;
    }

    private String readServerCertPem() throws IOException
    {
        return readPem( getHttpServerCertPemFile() );
    }

    private String readPem( String pemFile ) throws IOException
    {
        File f = new File( pemFile );
        if ( f.exists() )
        {
            return FileUtils.readFileToString( f, "UTF-8" );
        }

        return null;
    }

    private String readHttpKeyCertPem() throws IOException
    {
        return readPem( getHttpKeyCertPemFile() );
    }

    public String getHttpUri()
    {
        return httpUri;
    }

    public String getZabbixHost()
    {
        return zabbixHost;
    }

    public int getJsonRpcPort()
    {
        return jsonRpcPort;
    }

    public int getJsonRpcSocketTimeout()
    {
        return jsonRpcSocketTimeout;
    }

    public int getJsonRpcConnectionTimeout()
    {
        return jsonRpcConnectionTimeout;
    }

    public String getLocalHostName()
    {
        return localHostName;
    }

    @ConfigName( "local.host.name" )
    public void setLocalHostName( String localHostName )
    {
        this.localHostName = localHostName;
    }

    public String getHostGroup()
    {
        return hostGroup;
    }

    @ConfigName( "zabbix.host.group" )
    public void setHostGroup( String hostGroup )
    {
        this.hostGroup = hostGroup;
    }

    public boolean isAutoCreate()
    {
        return autoCreate == null ? DEFAULT_AUTO_CREATE : autoCreate;
    }

    public String getLocalIpAddress()
    {
        return localIpAddress;
    }

    @ConfigName( "local.ip.address" )
    public void setLocalIpAddress( String localIpAddress )
    {
        this.localIpAddress = localIpAddress;
    }

    @ConfigName( "zabbix.host.name" )
    public void setZabbixHost( String zabbixHost )
    {
        this.zabbixHost = zabbixHost;
    }

    @ConfigName( "rpc.port" )
    public void setJsonRpcPort( int jsonRpcPort )
    {
        this.jsonRpcPort = jsonRpcPort;
    }

    @ConfigName( "http.uri" )
    public void setHttpUri( String httpUri )
    {
        this.httpUri = httpUri;
    }

    @ConfigName( "rpc.socket.timeout" )
    public void setJsonRpcSocketTimeout( int jsonRpcSocketTimeout )
    {
        this.jsonRpcSocketTimeout = jsonRpcSocketTimeout;
    }

    @ConfigName( "rpc.connection.timeout" )
    public void setJsonRpcConnectionTimeout( int jsonRpcConnectionTimeout )
    {
        this.jsonRpcConnectionTimeout = jsonRpcConnectionTimeout;
    }

    @ConfigName( "autocreate" )
    public void setAutoCreate( Boolean autoCreate )
    {
        this.autoCreate = autoCreate;
    }

    public String getHttpUser()
    {
        return httpUser;
    }

    @ConfigName( "http.user" )
    public void setHttpUser( String httpUser )
    {
        this.httpUser = httpUser;
    }

    public Integer getHttpRequestTimeoutSeconds()
    {
        return httpRequestTimeoutSeconds;
    }

    @ConfigName( "http.request.timeout" )
    public void setHttpRequestTimeoutSeconds( Integer httpRequestTimeoutSeconds )
    {
        this.httpRequestTimeoutSeconds = httpRequestTimeoutSeconds;
    }

    public Integer getHttpConnectionTimeoutSeconds()
    {
        return httpConnectionTimeoutSeconds;
    }

    @ConfigName( "http.connection.timeout" )
    public void setHttpConnectionTimeoutSeconds( Integer httpConnectionTimeoutSeconds )
    {
        this.httpConnectionTimeoutSeconds = httpConnectionTimeoutSeconds;
    }

    public Integer getHttpMaxConnections()
    {
        return httpMaxConnections;
    }

    @ConfigName( "http.max.connections" )
    public void setHttpMaxConnections( Integer httpMaxConnections )
    {
        this.httpMaxConnections = httpMaxConnections;
    }

    public String getHttpServerCertPemFile()
    {
        return httpServerCertPemFile;
    }

    @ConfigName( "server.cert.pem" )
    public void setHttpServerCertPemFile( String httpServerCertPemFile )
    {
        this.httpServerCertPemFile = httpServerCertPemFile;
    }

    public String getHttpKeyCertPemFile()
    {
        return httpKeyCertPemFile;
    }

    @ConfigName( "keycert.pem" )
    public void setHttpKeyCertPemFile( String httpKeyCertPemFile )
    {
        this.httpKeyCertPemFile = httpKeyCertPemFile;
    }
}
