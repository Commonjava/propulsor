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
package org.commonjava.propulsor.config.fixture;

import org.commonjava.propulsor.config.annotation.ConfigName;

public class TestRoot
{
    private String keyOne;

    private String keyTwo;

    public String getKeyOne()
    {
        return keyOne;
    }

    @ConfigName( "key.one" )
    public void setKeyOne( final String keyOne )
    {
        this.keyOne = keyOne;
    }

    public String getKeyTwo()
    {
        return keyTwo;
    }

    @ConfigName( "key.two" )
    public void setKeyTwo( final String keyTwo )
    {
        this.keyTwo = keyTwo;
    }

}
