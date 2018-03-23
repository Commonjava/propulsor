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
package org.commonjava.web.config.section;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xbean.recipe.ObjectRecipe;
import org.commonjava.web.config.ConfigurationException;
import org.commonjava.web.config.annotation.ConfigName;
import org.commonjava.web.config.annotation.ConfigNames;

public class BeanSectionListener<T>
    implements TypedConfigurationSectionListener<T>
{

    private ObjectRecipe recipe;

    private final Class<T> type;

    private T instance;

    private final List<String> constructorArgs = new ArrayList<String>();

    private final Map<String, String> propertyMap = new HashMap<String, String>();

    public BeanSectionListener( final Class<T> type )
    {
        this.type = type;
        this.instance = null;

        doDiscovery( type );
    }

    @SuppressWarnings( "unchecked" )
    public BeanSectionListener( final T instance )
    {
        this.type = (Class<T>) instance.getClass();
        this.instance = instance;

        doDiscovery( type );
    }

    private void doDiscovery( final Class<T> type )
    {
        List<String> ctorArgs = null;
        for ( final Constructor<?> ctor : type.getConstructors() )
        {
            final ConfigNames names = ctor.getAnnotation( ConfigNames.class );
            if ( names != null )
            {
                if ( ctorArgs != null )
                {
                    throw new IllegalArgumentException( "Only one constructor can be annotated with @ConfigNames!" );
                }
                else if ( names.value().length != ctor.getParameterTypes().length )
                {
                    throw new IllegalArgumentException(
                                                        "Invalid number of configuration names in @ConfigNames annotation. Expected: "
                                                            + ctor.getParameterTypes().length + ", got: "
                                                            + names.value().length );
                }

                ctorArgs = new ArrayList<String>( Arrays.asList( names.value() ) );
            }
        }

        for ( final Method meth : type.getMethods() )
        {
            final ConfigName cn = meth.getAnnotation( ConfigName.class );

            if ( cn == null )
            {
                continue;
            }

            final String name = meth.getName();
            //            System.out.println( "Adding configuration property: " + name );

            if ( !( Modifier.isPublic( meth.getModifiers() ) && name.length() > 3 && name.startsWith( "set" ) ) )
            {
                throw new IllegalArgumentException(
                                                    "Invalid configuration method; not accessible, not a setter, or not a valid property name: "
                                                        + type.getClass()
                                                              .getName() + "." + name );
            }

            String propertyName = name.substring( 3 );
            if ( propertyName.length() > 1 )
            {
                propertyName = Character.toLowerCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 );
            }
            else
            {
                propertyName = propertyName.toLowerCase();
            }

            propertyMap.put( cn.value(), propertyName );
        }

        constructorArgs.clear();
        if ( ctorArgs != null && !ctorArgs.isEmpty() )
        {
            constructorArgs.addAll( ctorArgs );
        }

        recipe = new ObjectRecipe( type );
        if ( constructorArgs != null && !constructorArgs.isEmpty() )
        {
            recipe.setConstructorArgNames( constructorArgs );
        }
    }

    @Override
    public void sectionStarted( final String name )
        throws ConfigurationException
    {
    }

    @Override
    public void parameter( final String name, final String value )
        throws ConfigurationException
    {
        String realName = name;
        if ( ( constructorArgs == null || !constructorArgs.contains( name ) ) && propertyMap.containsKey( name ) )
        {
            realName = propertyMap.get( name );
        }

        recipe.setProperty( realName, value );
    }

    @Override
    public void sectionComplete( final String name )
        throws ConfigurationException
    {
        if ( instance != null )
        {
            recipe.setProperties( instance );
        }
        else
        {
            instance = type.cast( recipe.create() );
        }
    }

    @Override
    public synchronized T getConfiguration()
    {
        return instance;
    }

    @Override
    public String toString()
    {
        return String.format( "BeanSectionListener [type: %s, instance: %s]", type.getName(), instance );
    }

    @Override
    public Class<T> getConfigurationType()
    {
        return type;
    }

}
