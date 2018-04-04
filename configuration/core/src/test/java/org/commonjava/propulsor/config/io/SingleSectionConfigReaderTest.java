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
package org.commonjava.propulsor.config.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.commonjava.propulsor.config.ConfigurationException;
import org.commonjava.propulsor.config.fixture.TestChild;
import org.commonjava.propulsor.config.fixture.TestRoot;
import org.commonjava.propulsor.config.section.BeanSectionListener;
import org.junit.Test;

public class SingleSectionConfigReaderTest
{

    @Test
    public void simpleBeanConfiguration()
            throws ConfigurationException, IOException
    {
        final BeanSectionListener<TestRoot> listener = new BeanSectionListener<TestRoot>( TestRoot.class );
        final SingleSectionConfigReader reader = new SingleSectionConfigReader( listener );

        final Properties p = new Properties();
        p.setProperty( "key.one", "valueOne" );
        p.setProperty( "key.two", "valueTwo" );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        p.store( baos, "" );

        reader.loadConfiguration( new ByteArrayInputStream( baos.toByteArray() ) );

        final TestRoot result = listener.getConfiguration();

        assertThat( result.getKeyOne(), equalTo( "valueOne" ) );
        assertThat( result.getKeyTwo(), equalTo( "valueTwo" ) );
    }

    @Test
    public void inheritedBeanConfiguration()
        throws ConfigurationException, IOException
    {
        final BeanSectionListener<TestChild> listener = new BeanSectionListener<TestChild>( TestChild.class );

        final SingleSectionConfigReader reader = new SingleSectionConfigReader( listener );

        final Properties p = new Properties();
        p.setProperty( "key.one", "valueOne" );
        p.setProperty( "key.two", "valueTwo" );
        p.setProperty( "key.three", "valueThree" );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        p.store( baos, "" );

        reader.loadConfiguration( new ByteArrayInputStream( baos.toByteArray() ) );

        final TestChild result = listener.getConfiguration();

        assertThat( result.getKeyOne(), equalTo( "valueOne" ) );
        assertThat( result.getKeyTwo(), equalTo( "valueTwo" ) );
        assertThat( result.getKeyThree(), equalTo( "valueThree" ) );
    }

}
