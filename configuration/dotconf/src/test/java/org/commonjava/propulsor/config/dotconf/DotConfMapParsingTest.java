/**
 * Copyright (C) 2011 John Casey (jdcasey@commonjava.org)
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

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static org.apache.commons.io.IOUtils.writeLines;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.commonjava.propulsor.config.ConfigurationRegistry;
import org.commonjava.propulsor.config.DefaultConfigurationListener;
import org.commonjava.propulsor.config.DefaultConfigurationRegistry;
import org.commonjava.propulsor.config.dotconf.fixture.ListEx;
import org.commonjava.propulsor.config.dotconf.fixture.StringMap;
import org.commonjava.propulsor.config.section.ConfigurationSectionListener;
import org.commonjava.propulsor.config.section.MapSectionListener;
import org.junit.Test;

public class DotConfMapParsingTest
{

    @Test
    public void parseMap_TwoParameters()
        throws Exception
    {
        final List<String> lines = new ListEx( "one=foo", "two=bar" );
        final Map<String, String> check = new StringMap( "one", "foo", "two", "bar" );

        checkParse( lines, check, false );
    }

    @Test
    public void parseMap_ExactlyTwoParameters()
        throws Exception
    {
        final List<String> lines = new ListEx( "one=foo", "two=bar" );
        final Map<String, String> check = new StringMap( "one", "foo", "two", "bar" );

        checkParse( lines, check, true );
    }

    @Test
    public void parseMap_TwoParametersWithLastHavingTrailingComment()
        throws Exception
    {
        final List<String> lines = new ListEx( "one=foo", "two=bar # This is a comment" );
        final Map<String, String> check = new StringMap( "one", "foo", "two", "bar" );

        checkParse( lines, check, false );
    }

    @Test
    public void parseMap_TwoParametersWithFirstHavingTrailingComment()
        throws Exception
    {
        final List<String> lines = new ListEx( "one=foo # This is a comment", "two=bar" );
        final Map<String, String> check = new StringMap( "one", "foo", "two", "bar" );

        checkParse( lines, check, false );
    }

    @Test
    public void parseMap_TwoParametersWithLeadingCommentLine()
        throws Exception
    {
        final List<String> lines = new ListEx( "# This is a comment header.", "one=foo", "two=bar" );
        final Map<String, String> check = new StringMap( "one", "foo", "two", "bar" );

        checkParse( lines, check, true );
    }

    @Test
    public void parseMap_TwoParametersWithLeadingEmptyLine()
        throws Exception
    {
        final List<String> lines = new ListEx( "    ", "one=foo", "two=bar" );
        final Map<String, String> check = new StringMap( "one", "foo", "two", "bar" );

        checkParse( lines, check, true );
    }

    @Test
    public void parseMap_TwoParametersWithPrefixSpacing()
        throws Exception
    {
        final List<String> lines = new ListEx( "  one=foo", "  two=bar" );
        final Map<String, String> check = new StringMap( "one", "foo", "two", "bar" );

        checkParse( lines, check, false );
    }

    @Test
    public void parseMap_TwoParametersWithColonSeparators()
        throws Exception
    {
        final List<String> lines = new ListEx( "one: foo", "two: bar" );
        final Map<String, String> check = new StringMap( "one", "foo", "two", "bar" );

        checkParse( lines, check, false );
    }

    private void checkParse( final List<String> lines, final Map<String, String> check, final boolean strict )
        throws Exception
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        writeLines( lines, LINE_SEPARATOR, baos, "UTF-8" );

        final MapSectionListener sectionListener = new MapSectionListener();

        final ConfigurationRegistry registry =
            new DefaultConfigurationRegistry(
                                              new DefaultConfigurationListener().with( ConfigurationSectionListener.DEFAULT_SECTION,
                                                                                       sectionListener ) );

        final DotConfConfigurationReader reader = new DotConfConfigurationReader( registry );
        reader.loadConfiguration( new ByteArrayInputStream( baos.toByteArray() ) );

        final Map<String, String> result = sectionListener.getConfiguration();

        for ( final Map.Entry<String, String> entry : check.entrySet() )
        {
            assertThat( "Wrong value for: " + entry.getKey(), result.remove( entry.getKey() ),
                        equalTo( entry.getValue() ) );
        }

        if ( strict )
        {
            assertThat( "Parsed configuration has " + result.size() + " extra parameters: " + result, result.size(),
                        is( 0 ) );
        }
    }

}
