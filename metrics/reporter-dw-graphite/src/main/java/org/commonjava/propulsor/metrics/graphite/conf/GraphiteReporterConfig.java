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
package org.commonjava.propulsor.metrics.graphite.conf;

import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;
import org.commonjava.propulsor.metrics.dropwizard.spi.ReporterConfiguration;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@SectionName( "metrics.dropwizard.graphite" )
public class GraphiteReporterConfig
        extends ReporterConfiguration<GraphiteReporterConfig>
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
