package org.commonjava.propulsor.deploy.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.main.Main;
import org.commonjava.propulsor.boot.BootOptions;
import org.commonjava.propulsor.boot.BootStatus;
import org.commonjava.propulsor.deploy.Deployer;
import org.commonjava.propulsor.deploy.camel.ctx.CamelContextualizer;
import org.commonjava.propulsor.deploy.camel.route.RouteProvider;
import org.commonjava.propulsor.lifecycle.AppLifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Responsible for setting up Camel context, configuring it, setting up routes, and starting the Camel application.
 */
@ApplicationScoped
public class CamelDeployer
        implements Deployer
{

    private static final int ERR_CANT_CONTEXTUALIZE = 6;

    private static final int ERR_CANT_SETUP_ROUTES = 7;

    private Main camelMain;

    @Inject
    private Instance<RouteProvider> routeBuilders;

    @Inject
    private Instance<CamelContextualizer> contextualizers;

    private CamelContext context;

    @Produces
    public Main getCamelMain()
    {
        return camelMain;
    }

    @Produces
    public synchronized CamelContext getCamelContext()
    {
        if ( context == null )
        {
            context = camelMain.getOrCreateCamelContext();
        }

        return context;
    }

    @Override
    public void stop()
    {
        try
        {
            camelMain.shutdown();
        }
        catch ( Exception e )
        {
            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.error( "Camel shutdown failed.", e );
        }
    }

    @Override
    public BootStatus deploy( final BootOptions options )
    {
        camelMain = new Main();

        if ( contextualizers != null )
        {
            for ( CamelContextualizer ccc : contextualizers )
            {
                try
                {
                    ccc.contextualize( getCamelContext() );
                }
                catch ( AppLifecycleException e )
                {
                    return new BootStatus( ERR_CANT_CONTEXTUALIZE, e );
                }
            }
        }

        if ( routeBuilders != null )
        {
            for ( RouteProvider rb : routeBuilders )
            {
                try
                {
                    rb.addRoutesToCamelContext( getCamelContext() );
                }
                catch ( Exception e )
                {
                    return new BootStatus(ERR_CANT_SETUP_ROUTES, e );
                }
            }
        }

        try
        {
            camelMain.start();
        }
        catch ( Exception e )
        {
            return new BootStatus(BootStatus.ERR_STARTING, e );
        }

        return new BootStatus();
    }

}
