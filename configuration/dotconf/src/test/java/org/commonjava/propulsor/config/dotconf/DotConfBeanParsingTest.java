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
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.commonjava.propulsor.config.dotconf.fixture.ListEx;
import org.commonjava.propulsor.config.dotconf.fixture.Simpleton;
import org.commonjava.web.config.ConfigurationRegistry;
import org.commonjava.web.config.DefaultConfigurationListener;
import org.commonjava.web.config.DefaultConfigurationRegistry;
import org.commonjava.propulsor.config.dotconf.fixture.SimpletonInt;
import org.commonjava.web.config.section.BeanSectionListener;
import org.commonjava.web.config.section.ConfigurationSectionListener;
import org.junit.Test;

public class DotConfBeanParsingTest
{

    @Test
    public void parseBean_TwoParameters()
        throws Exception
    {
        final List<String> lines = new ListEx( "one=foo", "two=bar" );
        final Simpleton check = new Simpleton( "foo", "bar" );

        checkParse( lines, new BeanSectionListener<Simpleton>( Simpleton.class ), check );
    }

    @Test
    public void parseBean_TwoParametersCoerceSecondToInteger()
        throws Exception
    {
        final List<String> lines = new ListEx( "one=foo", "two=1" );
        final SimpletonInt check = new SimpletonInt( "foo", 1 );

        checkParse( lines, new BeanSectionListener<SimpletonInt>( SimpletonInt.class ), check );
    }

    @Test
    public void parseBean_TwoParametersWithLastHavingTrailingComment()
        throws Exception
    {
        final List<String> lines = new ListEx( "one=foo", "two=bar # This is a comment" );
        final Simpleton check = new Simpleton( "foo", "bar" );

        checkParse( lines, new BeanSectionListener<Simpleton>( Simpleton.class ), check );
    }

    @Test
    public void parseBean_TwoParametersWithFirstHavingTrailingComment()
        throws Exception
    {
        final List<String> lines = new ListEx( "one=foo # This is a comment", "two=bar" );
        final Simpleton check = new Simpleton( "foo", "bar" );

        checkParse( lines, new BeanSectionListener<Simpleton>( Simpleton.class ), check );
    }

    @Test
    public void parseBean_TwoParametersWithLeadingCommentLine()
        throws Exception
    {
        final List<String> lines = new ListEx( "# This is a comment header.", "one=foo", "two=bar" );
        final Simpleton check = new Simpleton( "foo", "bar" );

        checkParse( lines, new BeanSectionListener<Simpleton>( Simpleton.class ), check );
    }

    @Test
    public void parseBean_TwoParametersWithLeadingEmptyLine()
        throws Exception
    {
        final List<String> lines = new ListEx( "    ", "one=foo", "two=bar" );
        final Simpleton check = new Simpleton( "foo", "bar" );

        checkParse( lines, new BeanSectionListener<Simpleton>( Simpleton.class ), check );
    }

    @Test
    public void parseBean_TwoParametersWithPrefixSpacing()
        throws Exception
    {
        final List<String> lines = new ListEx( "  one=foo", "  two=bar" );
        final Simpleton check = new Simpleton( "foo", "bar" );

        checkParse( lines, new BeanSectionListener<Simpleton>( Simpleton.class ), check );
    }

    @Test
    public void parseBean_TwoParametersWithColonSeparators()
        throws Exception
    {
        final List<String> lines = new ListEx( "one: foo", "two: bar" );
        final Simpleton check = new Simpleton( "foo", "bar" );

        checkParse( lines, new BeanSectionListener<Simpleton>( Simpleton.class ), check );
    }

    private void checkParse( final List<String> lines, final BeanSectionListener<?> sectionListener, final Object check )
        throws Exception
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        writeLines( lines, LINE_SEPARATOR, baos );

        final ConfigurationRegistry registry =
            new DefaultConfigurationRegistry(
                                              new DefaultConfigurationListener().with( ConfigurationSectionListener.DEFAULT_SECTION,
                                                                                       sectionListener ) );

        final DotConfConfigurationReader reader = new DotConfConfigurationReader( registry );
        reader.loadConfiguration( new ByteArrayInputStream( baos.toByteArray() ) );

        final Object result = sectionListener.getConfiguration();

        assertThat( result, equalTo( check ) );
    }

}
