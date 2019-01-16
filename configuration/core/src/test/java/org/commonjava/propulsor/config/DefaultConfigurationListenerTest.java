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
package org.commonjava.propulsor.config;

import org.commonjava.propulsor.config.annotation.SectionName;
import org.commonjava.propulsor.config.fixture.TestRoot;
import org.commonjava.propulsor.config.section.MapSectionListener;
import org.junit.Test;

public class DefaultConfigurationListenerTest
{

    @Test
    public void testAnnotationsUsedIfSectionNameIsNull()
        throws ConfigurationException
    {
        new DefaultConfigurationListener().with( null, TestRoot.class )
                                          .with( null, new TestMapListener() );
    }

    @SectionName( "test" )
    public static final class TestMapListener
            extends MapSectionListener
    {

    }

}
