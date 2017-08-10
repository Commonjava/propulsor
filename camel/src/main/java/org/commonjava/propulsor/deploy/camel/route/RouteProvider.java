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
