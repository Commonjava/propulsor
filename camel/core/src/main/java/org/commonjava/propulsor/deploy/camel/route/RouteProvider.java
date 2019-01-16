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
package org.commonjava.propulsor.deploy.camel.route;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.BuilderSupport;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Provides routes during Camel application boot. To use, extend this class and implement the configure() method by calling
 * route() and using the fluent api it provides to setup a new route. Each time route() is called, a new route configuration
 * is created. Finally, after configure() exits, addRoutesToCamelContext() is called to add the configured routes to
 * the context.
 *
 * It is expected that your route endpoints will be abstracted keys, which will be used to lookup the actual endpoint
 * URI via the {@link #lookupEndpoint(String)} method below, as in: route().from(lookupEndpoint("my-endpoint"))...
 *
 * Subclasses should normally be initialized using CDI, which will inject a {@link EndpointAliasManager}. This allows
 * them to be used from {@link javax.enterprise.inject.Instance} injections. If constructing manually (as in a @Produces
 * factory method), please use the constructor that passes in the alias manager instance.
 */
public abstract class RouteProvider
        extends BuilderSupport
        implements RoutesBuilder
{
    private final RoutesDefinition routes = new RoutesDefinition();

    @Inject
    private EndpointAliasManager aliasManager;

    protected RouteProvider(){}

    protected RouteProvider( EndpointAliasManager aliasManager )
    {
        this.aliasManager = aliasManager;
    }

    @Override
    public final void addRoutesToCamelContext( final CamelContext context )
            throws Exception
    {
        for ( RouteDefinition route : routes.getRoutes() )
        {
            context.addRouteDefinition( route );
        }
    }

    protected abstract void configure();

    protected final String lookupEndpoint( String key )
    {
        return aliasManager.lookup( key );
    }

    protected final RouteDefinition route()
    {
        return routes.route();
    }
}
