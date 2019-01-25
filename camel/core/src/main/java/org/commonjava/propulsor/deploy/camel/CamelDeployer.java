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
package org.commonjava.propulsor.deploy.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.main.Main;
import org.commonjava.propulsor.boot.BootOptions;
import org.commonjava.propulsor.deploy.DeployException;
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
    public void deploy( final BootOptions options ) throws DeployException
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
                    throw new DeployException( "ERR_CANT_CONTEXTUALIZE", e );
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
                    throw new DeployException( "ERR_CANT_SETUP_ROUTES", e );
                }
            }
        }

        try
        {
            camelMain.start();
        }
        catch ( Exception e )
        {
            throw new DeployException( "ERR_STARTING", e );
        }
    }

}
