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
