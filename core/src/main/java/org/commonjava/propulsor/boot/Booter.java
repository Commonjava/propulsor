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

import org.commonjava.propulsor.config.Configurator;
import org.commonjava.propulsor.config.ConfiguratorException;
import org.commonjava.propulsor.deploy.DeployException;
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
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

import static org.commonjava.propulsor.boot.BootStatus.ERR_LOAD_BOOT_OPTIONS;
import static org.commonjava.propulsor.boot.BootStatus.ERR_PARSE_ARGS;
import static org.commonjava.propulsor.boot.BootStatus.ERR_START;

public class Booter
                implements WeldBootInterface
{
    public static String BOOT_DEFAULTS_PROP = "boot.defaults";

    public static void main( final String[] args )
    {
        setUncaughtExceptionHandler();

        BootOptions options = null;
        try
        {
            options = loadBootOptionsByServiceLoader();
        }
        catch ( final BootException e )
        {
            e.printStackTrace();
            System.err.printf( "ERR LOAD BOOT OPTIONS: %s", e.getMessage() );
            System.exit( ERR_LOAD_BOOT_OPTIONS );
        }

        try
        {
            if ( options.parseArgs( args ) )
            {
                try
                {
                    Booter booter = new Booter();
                    booter.runAndWait( options );
                }
                catch ( final BootException e )
                {
                    e.printStackTrace();
                    System.err.printf( "ERR START: %s", e.getMessage() );
                    System.exit( ERR_START );
                }
            }
        }
        catch ( final BootException e )
        {
            e.printStackTrace();
            System.err.printf( "ERR PARSE ARGS: %s", e.getMessage() );
            System.exit( ERR_PARSE_ARGS );
        }
    }

    public static BootOptions loadFromSysProps( String name, String bootDefaultProp, String homeProp )
                    throws BootException
    {
        final String bootDef = System.getProperty( bootDefaultProp );
        File bootDefaults = null;
        if ( bootDef != null )
        {
            bootDefaults = new File( bootDef );
        }

        try
        {
            final String indyHome = System.getProperty( homeProp, new File( "." ).getCanonicalPath() );
            return new BootOptions( name, indyHome, bootDefaults );
        }
        catch ( final Exception e )
        {
            throw new BootException( "ERROR LOADING BOOT DEFAULTS: %s.\nReason: %s\n\n", e, bootDefaults,
                                     e.getMessage() );
        }
    }

    public static BootOptions loadBootOptionsByServiceLoader() throws BootException
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
            String home = System.getenv( options.getHomeEnvar() );
            if ( home == null )
            {
                home = System.getProperty( options.getHomeSystemProperty(), new File( "." ).getCanonicalPath() );
            }
            options.setHomeDir( home );

            options.load( bootDefaults );
            return options;
        }
        catch ( final Exception e )
        {
            throw new BootException( "ERROR LOADING BOOT DEFAULTS: %s.\nReason: %s\n\n", e, bootDefaults,
                                     e.getMessage() );
        }
    }

    public static void setUncaughtExceptionHandler()
    {
        Thread.currentThread().setUncaughtExceptionHandler( ( thread, error ) -> {
            if ( error instanceof InvocationTargetException )
            {
                final InvocationTargetException ite = (InvocationTargetException) error;
                System.err.println( "In: " + thread.getName() + "(" + thread.getId()
                                                    + "), caught InvocationTargetException:" );
                ite.getTargetException().printStackTrace();

                System.err.println( "...via:" );
                error.printStackTrace();
            }
            else
            {
                System.err.println( "In: " + thread.getName() + "(" + thread.getId() + ") Uncaught error:" );
                error.printStackTrace();
            }
        } );
    }

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    protected BootStatus status;

    protected BootOptions options;

    protected Weld weld;

    protected WeldContainer container;

    protected Deployer deployer;

    protected Configurator configurator;

    protected AppLifecycleManager lifecycleManager;

    @Override
    public void initialize( final BootOptions options ) throws BootException
    {
        this.options = options;
        this.options.setSystemProperties();

        weld = new Weld();
        weld.property( "org.jboss.weld.se.archive.isolation", false );

        // Weld shutdown hook might disturb application shutdown hooks. We need to disable it.
        weld.skipShutdownHook();

        container = weld.initialize();

        // injectable version.
        final BootOptions cdiOptions = container.select( BootOptions.class ).get();
        if ( cdiOptions != null )
        {
            cdiOptions.copyFrom( options );
        }

        final BeanManager bmgr = container.getBeanManager();
        logger.info( "\n\n\nStarted BeanManager: {}\n\n\n", bmgr );
    }

    @Override
    public void runAndWait( final BootOptions bootOptions ) throws BootException
    {
        start( bootOptions );
        addNotifyShutDownHook();

        logger.info( "Start waiting on {}", this );
        synchronized ( this )
        {
            try
            {
                wait();
            }
            catch ( final InterruptedException e )
            {
                e.printStackTrace();
                logger.info( "{} exiting", bootOptions.getApplicationName() );
            }
        }
    }

    private void addNotifyShutDownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            synchronized ( this )
            {
                notifyAll();
            }
        } ) );
    }

    public WeldContainer getContainer()
    {
        return container;
    }

    public BootOptions getBootOptions()
    {
        return options;
    }

    protected Deployer getDeployer()
    {
        return deployer;
    }

    protected Configurator getConfigurator()
    {
        return configurator;
    }

    @Override
    public void start( final BootOptions bootOptions ) throws BootException
    {
        logger.info( "Starting, bootOptions: {}", bootOptions );
        try
        {
            logger.info( "Initializing..." );
            initialize( bootOptions );
        }
        catch ( final Throwable t )
        {
            throw new BootException( "Failed to initialize", t );
        }

        logger.info( "Configuring..." );
        try
        {
            config();
        }
        catch ( final ConfiguratorException e )
        {
            throw new BootException( "Application config failed", e );
        }

        logger.info( "Lifecycle..." );
        try
        {
            startLifecycle();
        }
        catch ( AppLifecycleException e )
        {
            throw new BootException( "Application startup failed", e );
        }

        logger.info( "Deploying..." );
        deploy();

        logger.info( "Start complete!" );
    }

    @Override
    public void config() throws ConfiguratorException
    {
        final Instance<Configurator> selection = container.select( Configurator.class );
        if ( !selection.iterator().hasNext() )
        {
            logger.info( "No configurator found!" );
            return;
        }

        configurator = selection.get();
        logger.info( "Configurator: {}", configurator.getClass() );

        configurator.load( options );
    }

    @Override
    public void startLifecycle() throws AppLifecycleException
    {
        final Instance<AppLifecycleManager> selection = container.select( AppLifecycleManager.class );
        if ( !selection.iterator().hasNext() )
        {
            logger.info( "No application life cycle manager found!" );
            return;
        }

        lifecycleManager = selection.get();
        logger.info( "LifecycleManager: {}", lifecycleManager.getClass() );

        lifecycleManager.startup();
        lifecycleManager.installShutdownHook();
    }

    @Override
    public void deploy() throws DeployException
    {
        final Instance<Deployer> selection = container.select( Deployer.class );
        if ( !selection.iterator().hasNext() )
        {
            logger.info( "No deployer found!" );
            return;
        }
        deployer = selection.get();
        logger.info( "Deployer: {}", deployer.getClass() );

        deployer.deploy( options );
    }

    @Override
    public void stop()
    {
        if ( deployer != null )
        {
            deployer.stop();
        }
        if ( lifecycleManager != null )
        {
            lifecycleManager.stop();
        }
        if ( weld != null )
        {
            weld.shutdown();
        }
    }

}
