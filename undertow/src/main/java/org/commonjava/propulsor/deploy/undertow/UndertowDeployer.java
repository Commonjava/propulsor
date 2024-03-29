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
package org.commonjava.propulsor.deploy.undertow;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.commonjava.propulsor.boot.BootOptions;
import org.commonjava.propulsor.boot.PortFinder;
import org.commonjava.propulsor.deploy.DeployException;
import org.commonjava.propulsor.deploy.Deployer;
import org.commonjava.propulsor.deploy.undertow.util.DeploymentInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class UndertowDeployer
    implements Deployer
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private Instance<UndertowDeploymentProvider> deployments;

    @Inject
    private Instance<UndertowDeploymentDefaultsProvider> defaultsDeployerInstance;

    @Inject
    private Instance<UndertowHandlerChain> handlerChain;

    private Set<UndertowDeploymentProvider> deploymentProviders;

    private UndertowDeploymentDefaultsProvider deploymentDefaultsProvider;

    private Undertow server;

    protected UndertowDeployer()
    {
    }

    public UndertowDeployer( final Set<UndertowDeploymentProvider> deploymentProviders,
                             final UndertowDeploymentDefaultsProvider deploymentDefaultsProvider )
    {
        this.deploymentProviders = deploymentProviders;
        this.deploymentDefaultsProvider = deploymentDefaultsProvider;
    }

    @PostConstruct
    public void cdiInit()
    {
        deploymentProviders = new HashSet<>();
        for ( final UndertowDeploymentProvider fac : deployments )
        {
            logger.debug("Add deployment: " + fac.toString());
            deploymentProviders.add( fac );
        }

        if ( !defaultsDeployerInstance.isUnsatisfied() )
        {
            deploymentDefaultsProvider = defaultsDeployerInstance.get();
        }
    }

    public DeploymentInfo getDeployment( final String contextRoot, final String deploymentName )
    {
        final DeploymentInfo di = new DeploymentInfo().setContextPath( contextRoot )
                                                      .setDeploymentName( deploymentName )
                                                      .setClassLoader( ClassLoader.getSystemClassLoader() );

        if ( deploymentDefaultsProvider != null )
        {
            deploymentDefaultsProvider.setDefaults( di );
        }

        if ( deploymentProviders != null )
        {
            DeploymentInfoUtils.mergeFromProviders( di, deploymentProviders );
        }

        return di;
    }

    @Override
    public void deploy( final BootOptions bootOptions ) throws DeployException
    {
        final DeploymentInfo di = getDeployment( bootOptions.getContextPath(), bootOptions.getApplicationName() );

        final DeploymentManager dm = Servlets.defaultContainer()
                                             .addDeployment( di );
        dm.deploy();

        ThreadLocal<Integer> usingPort = new ThreadLocal<>();
        try
        {
            Integer port = bootOptions.getPort();
            if ( port < 1 )
            {
                System.out.println("Looking for open port...");

                final AtomicReference<ServletException> errorHolder = new AtomicReference<>();
                server = PortFinder.findPortFor( 16, ( foundPort ) -> {
                    Undertow undertow = null;
                    try
                    {
                        usingPort.set( foundPort );
                        undertow = getUndertowServer( dm, foundPort, bootOptions );
                    }
                    catch ( ServletException e )
                    {
                        errorHolder.set( e );
                    }

                    return undertow;
                } );

                ServletException e = errorHolder.get();
                if ( e != null )
                {
                    throw e;
                }
                bootOptions.setPort( usingPort.get() );
            }
            else
            {
                usingPort.set( port );
                server = getUndertowServer( dm, port, bootOptions );
            }
            server.start();
            System.out.printf( "%s listening on %s:%s\n\n", bootOptions.getApplicationName(), bootOptions.getBind(), bootOptions.getPort() );

        }
        catch ( Exception e )
        {
            throw new DeployException( "Failed to deploy", e );
        }
    }

    private Undertow getUndertowServer( DeploymentManager dm, int foundPort, BootOptions bootOptions )
                    throws ServletException
    {
        logger.info( "Build Undertow with HTTP/2 enabled" );
        return Undertow.builder()
                       .setServerOption( UndertowOptions.ENABLE_HTTP2, true )
                       .setHandler( getHandler( dm ) )
                       .addHttpListener( foundPort, bootOptions.getBind() )
                       .build();
    }

    private HttpHandler getHandler( final DeploymentManager dm )
            throws ServletException
    {
        HttpHandler base = dm.start();

        if ( !handlerChain.isUnsatisfied() )
        {
            return handlerChain.get().getHandler( base );
        }

        // FROM: https://stackoverflow.com/questions/28295752/compressing-undertow-server-responses#28329810
        final Predicate sizePredicate =
                Predicates.parse( "max-content-size[" + Long.toString( 5 * 1024 ) + "]" );

        EncodingHandler eh = new EncodingHandler(
                new ContentEncodingRepository().addEncodingHandler( "gzip", new GzipEncodingProvider(),
                                                                    50, sizePredicate )
                                               .addEncodingHandler( "deflate",
                                                                    new DeflateEncodingProvider(), 51,
                                                                    sizePredicate ) ).setNext(
                base );

        return eh;
    }

    @Override
    public void stop()
    {
        if ( server != null )
        {
            server.stop();
        }
    }

}
