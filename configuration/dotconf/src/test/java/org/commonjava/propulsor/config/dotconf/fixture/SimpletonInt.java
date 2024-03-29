/**
 * Copyright (C) 2014-2022 Red Hat, Inc. (http://github.com/Commonjava/commonjava)
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
package org.commonjava.propulsor.config.dotconf.fixture;

import org.commonjava.propulsor.config.annotation.ConfigNames;
import org.commonjava.propulsor.config.annotation.SectionName;

@SectionName( "object" )
public class SimpletonInt
{

    private String one;

    private Integer two;

    public SimpletonInt()
    {

    }

    @ConfigNames( { "one", "two" } )
    public SimpletonInt( final String one, final Integer two )
    {
        this.one = one;
        this.two = two;
    }

    public String getOne()
    {
        return one;
    }

    public void setOne( final String one )
    {
        this.one = one;
    }

    public Integer getTwo()
    {
        return two;
    }

    public void setTwo( final Integer two )
    {
        this.two = two;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( one == null ) ? 0 : one.hashCode() );
        result = prime * result + ( ( two == null ) ? 0 : two.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final SimpletonInt other = (SimpletonInt) obj;
        if ( one == null )
        {
            if ( other.one != null )
            {
                return false;
            }
        }
        else if ( !one.equals( other.one ) )
        {
            return false;
        }
        if ( two == null )
        {
            if ( other.two != null )
            {
                return false;
            }
        }
        else if ( !two.equals( other.two ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return String.format( "Simpleton [one=%s, two=%s]", one, two );
    }

}
