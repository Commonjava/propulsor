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
package org.commonjava.propulsor.config.section;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.commonjava.propulsor.config.ConfigurationException;
import org.commonjava.propulsor.config.fixture.TestChild;
import org.commonjava.propulsor.config.fixture.TestRoot;
import org.junit.Test;

public class BeanSectionListenerTest
{

    @Test
    public void simpleBeanConfiguration()
            throws ConfigurationException
    {
        final BeanSectionListener<TestRoot> listener = new BeanSectionListener<TestRoot>( TestRoot.class );
        listener.sectionStarted( ConfigurationSectionListener.DEFAULT_SECTION );

        listener.parameter( "key.one", "valueOne" );
        listener.parameter( "key.two", "valueTwo" );

        listener.sectionComplete( ConfigurationSectionListener.DEFAULT_SECTION );

        final TestRoot result = listener.getConfiguration();

        assertThat( result.getKeyOne(), equalTo( "valueOne" ) );
        assertThat( result.getKeyTwo(), equalTo( "valueTwo" ) );
    }

    @Test
    public void inheritedBeanConfiguration()
        throws ConfigurationException
    {
        final BeanSectionListener<TestChild> listener = new BeanSectionListener<TestChild>( TestChild.class );
        listener.sectionStarted( ConfigurationSectionListener.DEFAULT_SECTION );

        listener.parameter( "key.one", "valueOne" );
        listener.parameter( "key.two", "valueTwo" );
        listener.parameter( "key.three", "valueThree" );

        listener.sectionComplete( ConfigurationSectionListener.DEFAULT_SECTION );

        final TestChild result = listener.getConfiguration();

        assertThat( result.getKeyOne(), equalTo( "valueOne" ) );
        assertThat( result.getKeyTwo(), equalTo( "valueTwo" ) );
        assertThat( result.getKeyThree(), equalTo( "valueThree" ) );
    }

}
