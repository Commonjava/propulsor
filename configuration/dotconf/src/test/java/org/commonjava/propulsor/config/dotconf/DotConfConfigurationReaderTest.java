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
package org.commonjava.propulsor.config.dotconf;

import org.commonjava.propulsor.config.ConfigurationRegistry;
import org.commonjava.propulsor.config.DefaultConfigurationListener;
import org.commonjava.propulsor.config.DefaultConfigurationRegistry;
import org.commonjava.propulsor.config.dotconf.fixture.ListEx;
import org.commonjava.propulsor.config.dotconf.fixture.SimpletonInt;
import org.commonjava.propulsor.config.section.BeanSectionListener;
import org.commonjava.propulsor.config.section.MapSectionListener;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static org.apache.commons.io.IOUtils.writeLines;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DotConfConfigurationReaderTest
{

    @Test
    public void readTwoSectionsOneWithMapParserTheOtherWithBeanParserUsingCoercion()
        throws Exception
    {
        final List<String> lines =
            new ListEx( "[mappings]", "newUser: templates/custom-newUser", "changePassword: templates/change-password", "", "", "[object]",
                        "one=foo", "two: 2" );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeLines( lines, LINE_SEPARATOR, baos, "UTF-8" );

        final DefaultConfigurationListener configListener =
            new DefaultConfigurationListener( new BeanSectionListener<SimpletonInt>( SimpletonInt.class ) ).with( "mappings",
                                                                                                                  new MapSectionListener() );

        final ConfigurationRegistry dispatcher = new DefaultConfigurationRegistry( configListener );
        final DotConfConfigurationReader reader = new DotConfConfigurationReader( dispatcher );

        reader.loadConfiguration( new ByteArrayInputStream( baos.toByteArray() ) );

        assertThat( (String) configListener.getConfiguration( "mappings", Map.class )
                                           .get( "newUser" ), equalTo( "templates/custom-newUser" ) );
        assertThat( (String) configListener.getConfiguration( "mappings", Map.class )
                                           .get( "changePassword" ), equalTo( "templates/change-password" ) );
        assertThat( configListener.getConfiguration( "object", SimpletonInt.class ), equalTo( new SimpletonInt( "foo", 2 ) ) );
    }

    @Test
    public void readTwoSectionsOneWithMapParserTheOtherWithAnnotatedBeanParser()
        throws Exception
    {
        final List<String> lines =
            new ListEx( "[mappings]", "newUser: templates/custom-newUser", "changePassword: templates/change-password", "", "", "[object]",
                        "one=foo", "two: 2" );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeLines( lines, LINE_SEPARATOR, baos );

        final DefaultConfigurationListener configListener =
            new DefaultConfigurationListener( new BeanSectionListener<SimpletonInt>( SimpletonInt.class ) ).with( "mappings",
                                                                                                                  new MapSectionListener() );

        final ConfigurationRegistry dispatcher = new DefaultConfigurationRegistry( configListener );
        final DotConfConfigurationReader reader = new DotConfConfigurationReader( dispatcher );

        reader.loadConfiguration( new ByteArrayInputStream( baos.toByteArray() ) );

        assertThat( (String) configListener.getConfiguration( "mappings", Map.class )
                                           .get( "newUser" ), equalTo( "templates/custom-newUser" ) );
        assertThat( (String) configListener.getConfiguration( "mappings", Map.class )
                                           .get( "changePassword" ), equalTo( "templates/change-password" ) );
        assertThat( configListener.getConfiguration( "object", SimpletonInt.class ), equalTo( new SimpletonInt( "foo", 2 ) ) );
    }

    @Test
    public void readOneSectionWithMapParserAndLineContinuation()
        throws Exception
    {
        final List<String> lines = new ListEx( "[mappings]", "newUser: templates/custom-newUser \\" + "\n        Testing" );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeLines( lines, LINE_SEPARATOR, baos );

        final DefaultConfigurationListener configListener = new DefaultConfigurationListener().with( "mappings", new MapSectionListener() );

        final ConfigurationRegistry dispatcher = new DefaultConfigurationRegistry( configListener );
        final DotConfConfigurationReader reader = new DotConfConfigurationReader( dispatcher );

        reader.loadConfiguration( new ByteArrayInputStream( baos.toByteArray() ) );

        assertThat( (String) configListener.getConfiguration( "mappings", Map.class )
                                           .get( "newUser" ), equalTo( "templates/custom-newUser Testing" ) );
    }

    @Test
    public void readOneSectionWithMapParserAndCompoundValue()
        throws Exception
    {
        final String testValue = "templates/custom-newUser;test=true";
        final List<String> lines = new ListEx( "[mappings]", "newUser=" + testValue );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeLines( lines, LINE_SEPARATOR, baos );

        final DefaultConfigurationListener configListener =
            new DefaultConfigurationListener().with( "mappings", new MapSectionListener() );

        final ConfigurationRegistry dispatcher = new DefaultConfigurationRegistry( configListener );
        final DotConfConfigurationReader reader = new DotConfConfigurationReader( dispatcher );

        reader.loadConfiguration( new ByteArrayInputStream( baos.toByteArray() ) );

        assertThat( (String) configListener.getConfiguration( "mappings", Map.class )
                                           .get( "newUser" ), equalTo( testValue ) );
    }

}
