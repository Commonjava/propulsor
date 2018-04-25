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
package org.commonjava.propulsor.boot;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.commonjava.propulsor.config.Configurator;
import org.commonjava.propulsor.config.ConfiguratorException;
import org.commonjava.propulsor.deploy.Deployer;
import org.commonjava.propulsor.lifecycle.AppLifecycleException;
import org.commonjava.propulsor.lifecycle.AppLifecycleManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

import static org.commonjava.propulsor.boot.BootOptions.BOOT_DEFAULTS_PROP;
import static org.commonjava.propulsor.boot.BootStatus.ERR_CANT_INIT_BOOTER;
import static org.commonjava.propulsor.boot.BootStatus.ERR_LOAD_CONFIG;
import static org.commonjava.propulsor.boot.BootStatus.ERR_LOAD_FROM_SYSPROPS;
import static org.commonjava.propulsor.boot.BootStatus.ERR_PARSE_ARGS;
import static org.commonjava.propulsor.boot.BootStatus.ERR_STARTING;

public class Booter
{
    public static void main( final String[] args )
    {
        Thread.currentThread()
              .setUncaughtExceptionHandler( (thread,error) ->
                  {
                      if ( error instanceof InvocationTargetException )
                      {
                          final InvocationTargetException ite = (InvocationTargetException) error;
                          System.err.println( "In: " + thread.getName() + "(" + thread.getId()
                                                      + "), caught InvocationTargetException:" );
                          ite.getTargetException()
                             .printStackTrace();

                          System.err.println( "...via:" );
                          error.printStackTrace();
                      }
                      else
                      {
                          System.err.println( "In: " + thread.getName() + "(" + thread.getId() + ") Uncaught error:" );
                          error.printStackTrace();
                      }
                  } );

        BootOptions options = null;
        try
        {
            options = loadBootOptions();
        }
        catch ( final BootException e )
        {
            e.printStackTrace();
            System.err.println( e.getMessage() );
            System.exit( ERR_LOAD_FROM_SYSPROPS );
        }

        try
        {
            options.parseArgs( args );
        }
        catch ( final BootException e )
        {
            e.printStackTrace();
            System.err.println( e.getMessage() );
            System.exit( ERR_PARSE_ARGS );
        }

        BootStatus status = null;
        try
        {
            status = new Booter().runAndWait( options );
        }
        catch ( final BootException e )
        {
            e.printStackTrace();
            System.err.println( e.getMessage() );
            System.exit( ERR_STARTING );
        }

        if ( status.isFailed() )
        {
            status.getError().printStackTrace();
            System.err.println( status.getError().getMessage() );
            System.exit( status.getExitCode() );
        }
    }

    private static BootOptions loadBootOptions()
            throws BootException
    {
        final String bootDef = System.getProperty( BOOT_DEFAULTS_PROP );
        File bootDefaults = null;
        if ( bootDef != null )
        {
            bootDefaults = new File( bootDef );
        }

        final ServiceLoader<BootOptions> loader = ServiceLoader.load( BootOptions.class );
        final BootOptions options = loader.iterator().next();

        try
        {
            String home = System.getProperty( options.getHomeSystemProperty() );

            if ( home == null )
            {
                home = System.getenv( options.getHomeEnvar() );
            }

            if ( home == null )
            {
                home = new File( "." ).getCanonicalPath();
            }

            options.load( bootDefaults, home );
            return options;
        }
        catch ( final IOException e )
        {
            throw new BootException( "ERROR LOADING BOOT DEFAULTS: %s.\nReason: %s\n\n", e, bootDefaults,
                                     e.getMessage() );
        }
        catch ( final InterpolationException e )
        {
            throw new BootException( "ERROR RESOLVING BOOT DEFAULTS: %s.\nReason: %s\n\n", e, bootDefaults,
                                     e.getMessage() );
        }
    }

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private BootStatus status;

    private BootOptions options;

    private Weld weld;

    private WeldContainer container;

    private Deployer deployer;

    private Configurator configurator;

    private AppLifecycleManager lifecycleManager;

    public BootStatus initialize( final BootOptions options )
            throws BootException
    {
        this.options = options;

        try
        {
            options.setSystemProperties();

            weld = new Weld();
            container = weld.initialize();

            // injectable version.
            final BootOptions cdiOptions = container.instance()
                                                        .select( BootOptions.class )
                                                        .get();
            cdiOptions.copyFrom( options );

            final BeanManager bmgr = container.getBeanManager();
            logger.info( "\n\n\nStarted BeanManager: {}\n\n\n", bmgr );
        }
        catch ( final RuntimeException e )
        {
            logger.error( "Failed to initialize Booter: " + e.getMessage(), e );
            status = new BootStatus( ERR_CANT_INIT_BOOTER, e );
        }

        return status;
    }

    public BootStatus runAndWait( final BootOptions bootOptions )
            throws BootException
    {
        status = start( bootOptions );
        if ( !status.isSuccess() )
        {
            return status;
        }

        logger.info( "Setting up shutdown hook..." );
        lifecycleManager.installShutdownHook();

        synchronized ( deployer )
        {
            try
            {
                deployer.wait();
            }
            catch ( final InterruptedException e )
            {
                e.printStackTrace();
                logger.info( "{} exiting", options.getApplicationName() );
            }
        }

        return status;
    }

    public BootStatus run( final BootOptions bootOptions )
            throws BootException
    {
        status = start( bootOptions );
        if ( !status.isSuccess() )
        {
            return status;
        }

        logger.info( "Setting up shutdown hook..." );
        lifecycleManager.installShutdownHook();

        return status;
    }

    public WeldContainer getContainer()
    {
        return container;
    }

    public BootOptions getBootOptions()
    {
        return options;
    }

    public BootStatus deploy()
    {
        deployer = container.instance().select( Deployer.class ).get();
        BootStatus status = deployer.deploy( options );

        return status == null ?
                new BootStatus( ERR_STARTING, new IllegalStateException( "Deployment failed" ) ) :
                status;
    }

    public BootStatus start( final BootOptions bootOptions )
            throws BootException
    {
        BootStatus status = initialize( bootOptions );
        if ( status != null )
        {
            return status;
        }

        logger.info( "Booter running: " + this );

        configure();
        if ( status != null )
        {
            return status;
        }

        try
        {
            startLifecycle();
        }
        catch ( AppLifecycleException e )
        {
            throw new BootException( "Application startup failed. Reason: %s", e, e.getMessage() );
        }

        return deploy();
    }

    public BootStatus configure()
    {
        final Instance<Configurator> selection = container.instance().select( Configurator.class );
        if ( !selection.iterator().hasNext() )
        {
            return null;
        }

        configurator = selection.get();
        try
        {
            configurator.load( options );
            return null;
        }
        catch ( final ConfiguratorException e )
        {
            status = new BootStatus( ERR_LOAD_CONFIG, e );
        }

        return status;
    }

    public void startLifecycle()
            throws AppLifecycleException
    {
        final Instance<AppLifecycleManager> selection = container.instance().select( AppLifecycleManager.class );
        if ( !selection.iterator().hasNext() )
        {
            return;
        }

        lifecycleManager = selection.get();

        logger.info( "Starting up application with lifecycle manager: {}", lifecycleManager );
        lifecycleManager.startup();
    }

    public BootStatus stop()
    {
        if ( status.isSuccess() )
        {
            if ( container != null )
            {
                deployer.stop();
                if ( lifecycleManager != null )
                {
                    lifecycleManager.stop();
                }
                weld.shutdown();
            }
        }

        return status;
    }
}
