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
package org.commonjava.propulsor.config.section;

import org.commonjava.propulsor.config.ConfigurationException;
import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.ConfigNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.commonjava.propulsor.config.section.Coercions.coerce;

public class BeanSectionListener<T>
                implements TypedConfigurationSectionListener<T>
{

    public static final String UNSET_PROPERTIES_MAP = "unset.properties";

    private Class<T> type;

    private T instance;

    private List<String> constructorArgs = new ArrayList<>();

    private final Map<String, MethodInvoker> methodMap = new HashMap<>();

    private ConstructorInvoker<T> constructorInvoker;

    private Map<String, String> params = new HashMap<>();

    private Method unsetPropertiesMethod;

    protected BeanSectionListener()
    {
        setupSelf();
    }

    private void setupSelf()
    {
        this.type = (Class<T>) this.getClass();
        this.instance = (T) this;
    }

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
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.debug( "Discovering configuration parameters for: {}", type );

        Constructor<T> empty = null;
        for ( final Constructor<?> ctor : type.getConstructors() )
        {
            final ConfigNames names = ctor.getAnnotation( ConfigNames.class );
            if ( names != null )
            {
                if ( constructorInvoker != null )
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

                logger.debug( "Found config constructor: {} with {} parameters", ctor, names.value().length );
                constructorArgs = new ArrayList<>( Arrays.asList( names.value() ) );
                constructorInvoker = new ConstructorInvoker<>( (Constructor<T>) ctor, constructorArgs.size() );
            }
            else if ( ctor.getParameterCount() == 0 )
            {
                empty = (Constructor<T>) ctor;
            }
        }

        if ( constructorInvoker == null && empty != null )
        {
            constructorInvoker = new ConstructorInvoker<>( empty, 0 );
            constructorArgs = Collections.emptyList();
        }

        Set<Class<?>> seen = new HashSet<>();
        Class<?> t = type;
        do
        {
            seen.add( t );

            logger.debug( "Scanning type: {}", t );
            for ( final Method meth : t.getMethods() )
            {
                final ConfigName cn = meth.getAnnotation( ConfigName.class );

                if ( cn == null )
                {
                    continue;
                }

                logger.debug( "Found configuration method: {}", meth );
                final String name = meth.getName();
                //            System.out.println( "Adding configuration property: " + name );

                if ( !( Modifier.isPublic( meth.getModifiers() ) && meth.getParameterCount() == 1 ) )
                {
                    throw new IllegalArgumentException(
                                    "Invalid configuration method; not accessible or has wrong parameter count: " + type
                                                    .getClass()
                                                    .getName() + "." + name );
                }
                else if ( UNSET_PROPERTIES_MAP.equals( cn.value() ) && Map.class.isAssignableFrom(
                                meth.getParameterTypes()[0] ) )
                {
                    logger.debug( "Configuration method is for unset-properties capture: {}", meth );
                    unsetPropertiesMethod = meth;
                }
                else
                {
                    logger.debug( "Configuration method {} is for property: {}", meth, cn.value() );
                    methodMap.put( cn.value(), new MethodInvoker( meth ) );
                }
            }

            t = type.getSuperclass();
        }
        while ( !Object.class.equals( t ) && !seen.contains( t ) );
    }

    @Override
    public void sectionStarted( final String name ) throws ConfigurationException
    {
        final Logger logger = LoggerFactory.getLogger( getClass() );
        logger.debug( "Starting config section: {}", name );
    }

    @Override
    public void parameter( final String name, final String value ) throws ConfigurationException
    {
        params.put( name, value );
    }

    @Override
    public void sectionComplete( final String section )
                    throws ConfigurationException
    {
        final Logger logger = LoggerFactory.getLogger( getClass() );
        logger.debug( "Completing config section: {}. Applying to configuration object.", section );

        final Map<String, String> unmatched = new HashMap<>();

        final Map<String, String> errors = new HashMap<>();
        params.forEach( (name,value)->{
            logger.debug( "Coercing parameter: {}", name );
            try
            {
                MethodInvoker inv = methodMap.get( name );
                if ( ( constructorArgs != null && constructorArgs.contains( name ) ) )
                {
                    logger.debug( "parameter is constructor param" );

                    int idx = constructorArgs.indexOf( name );
                    constructorInvoker.setArg( value, idx );
                }
                else if ( inv != null )
                {
                    logger.debug( "parameter is method param" );

                    inv.setParam( value );
                }
                else
                {
                    logger.debug( "parameter is unmatched" );

                    unmatched.put( name, value );
                }
            }
            catch ( ConfigurationException e )
            {
                logger.debug( "Error coercing! {}", e.getMessage() );

                errors.put( name, e.getMessage() );
            }
        } );

        if ( !errors.isEmpty() )
        {
            StringBuilder sb = new StringBuilder("Failed to configure parameters:\n");
            errors.forEach( (name,error)->sb.append("\n  - ").append(name).append(": ").append(error) );
            sb.append( "\n\n" );

            throw new ConfigurationException( sb.toString() );
        }

        if ( instance == null )
        {
            if ( constructorInvoker != null )
            {
                logger.debug( "Creating instance via constructorInvoker" );
                try
                {
                    instance = constructorInvoker.invoke();
                }
                catch ( IllegalAccessException | InstantiationException e )
                {
                    throw new ConfigurationException( "Failed to create configuration object: %s", e, e.getMessage() );
                }
                catch ( InvocationTargetException e )
                {
                    throw new ConfigurationException( "Failed to create configuration object: %s", e.getTargetException(), e.getTargetException().getMessage() );
                }
            }
            else
            {
                throw new ConfigurationException( "Cannot find suitable constructor for: ", type );
            }
        }

        for ( Map.Entry<String, MethodInvoker> entry : methodMap.entrySet() )
        {
            logger.debug( "Invoking: {}", entry.getValue().method );

            try
            {
                entry.getValue().invoke( instance );
            }
            catch ( InvocationTargetException e )
            {
                throw new ConfigurationException( "Failed to configure %s on %s: %s", e.getTargetException(),
                                                  entry.getKey(), instance, e.getTargetException().getMessage() );
            }
            catch ( IllegalAccessException e )
            {
                throw new ConfigurationException( "Failed to configure %s on %s: %s", e, entry.getKey(), instance, e.getMessage() );
            }
        }

        if ( unsetPropertiesMethod != null && !unmatched.isEmpty() )
        {
            logger.debug( "Invoking unset-properties method: {}", unsetPropertiesMethod );

            try
            {
                unsetPropertiesMethod.invoke( instance, unmatched );
            }
            catch ( IllegalAccessException e )
            {
                throw new ConfigurationException( "Failed to configure unmatched properties on %s: %s", e,
                                                  instance, e.getMessage() );
            }
            catch ( InvocationTargetException e )
            {
                throw new ConfigurationException( "Failed to configure unmatched properties on %s: %s", e.getTargetException(),
                                                  instance, e.getTargetException().getMessage() );
            }
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

    private static final class MethodInvoker
    {
        private Method method;

        private Object param;

        MethodInvoker( Method method )
        {
            this.method = method;
        }

        void setParam( String param ) throws ConfigurationException
        {
            this.param = coerce( param, method.getParameterTypes()[0], method );
        }

        void invoke( Object instance ) throws InvocationTargetException, IllegalAccessException
        {
            method.invoke( instance, param );
        }
    }

    private static final class ConstructorInvoker<T>
    {
        private final Constructor<T> ctor;

        private final Object[] args;

        ConstructorInvoker( Constructor<T> ctor, int argCount )
        {
            this.ctor = ctor;
            this.args = new Object[argCount];
        }

        void setArg( String param, int idx) throws ConfigurationException
        {
            args[idx] = coerce( param, ctor.getParameterTypes()[idx], ctor );
        }

        T invoke() throws IllegalAccessException, InvocationTargetException, InstantiationException
        {
            return ctor.newInstance( args );
        }
    }
}
