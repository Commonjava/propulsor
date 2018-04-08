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
package org.commonjava.propulsor.config.dotconf.fixture;

import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;

@SectionName( "object" )
public class Simpleton
{

    private String one;

    private String two;

    public Simpleton()
    {

    }

    public Simpleton( final String one, final String two )
    {
        this.one = one;
        this.two = two;
    }

    public String getOne()
    {
        return one;
    }

    @ConfigName( "one" )
    public void setOne( final String one )
    {
        this.one = one;
    }

    public String getTwo()
    {
        return two;
    }

    @ConfigName( "two" )
    public void setTwo( final String two )
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
        final Simpleton other = (Simpleton) obj;
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
