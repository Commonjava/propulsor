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
