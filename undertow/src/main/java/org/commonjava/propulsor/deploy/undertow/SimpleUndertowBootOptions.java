package org.commonjava.propulsor.deploy.undertow;

import org.commonjava.propulsor.boot.BootOptions;

/**
 * Basic application configuration for use with Undertow.
 */
public final class SimpleUndertowBootOptions
        extends BootOptions
        implements UndertowBootOptions
{
    private final String appName;

    private final String configSysprop;

    private final String homeSysprop;

    private final String homeEnvar;

    private Integer port;

    public SimpleUndertowBootOptions( String appName, String configSysprop, String homeSysprop, String homeEnvar )
    {
        this.appName = appName;
        this.configSysprop = configSysprop;
        this.homeSysprop = homeSysprop;
        this.homeEnvar = homeEnvar;
    }

    @Override
    public String getContextPath()
    {
        return "/";
    }

    @Override
    public String getDeploymentName()
    {
        return "Web (Undertow)";
    }

    @Override
    public int getPort()
    {
        return port == null ? 8080 : port;
    }

    @Override
    public String getBind()
    {
        return "0.0.0.0";
    }

    @Override
    public void setPort( int port )
    {
        this.port = port;
    }

    @Override
    public String getApplicationName()
    {
        return appName;
    }

    @Override
    public String getHomeSystemProperty()
    {
        return homeSysprop;
    }

    @Override
    public String getConfigSystemProperty()
    {
        return configSysprop;
    }

    @Override
    public String getHomeEnvar()
    {
        return homeEnvar;
    }
}
