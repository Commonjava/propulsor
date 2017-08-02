package org.commonjava.propulsor.deploy.camel;

import org.apache.camel.main.Main;
import org.commonjava.propulsor.boot.BootOptions;
import org.commonjava.propulsor.boot.BootStatus;
import org.commonjava.propulsor.deploy.Deployer;
import org.commonjava.propulsor.deploy.camel.ctx.CamelContextualizer;
import org.commonjava.propulsor.deploy.camel.route.RoutingSetup;
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
    private Instance<RoutingSetup> routeBuilders;

    @Inject
    private Instance<CamelContextualizer> contextualizers;

    @Produces
    public Main getCamelMain()
    {
        return camelMain;
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
                    ccc.contextualize( camelMain.getOrCreateCamelContext() );
                }
                catch ( AppLifecycleException e )
                {
                    return new BootStatus( ERR_CANT_CONTEXTUALIZE, e );
                }
            }
        }

        if ( routeBuilders != null )
        {
            for ( RoutingSetup rb : routeBuilders )
            {
                try
                {
                    rb.addRoutesToCamelContext( camelMain.getOrCreateCamelContext() );
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
