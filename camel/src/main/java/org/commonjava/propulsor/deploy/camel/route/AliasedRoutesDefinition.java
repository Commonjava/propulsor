package org.commonjava.propulsor.deploy.camel.route;

import org.apache.camel.ErrorHandlerFactory;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;

/**
 * Wrap the normal {@link RoutesDefinition} by including an alias manager ({@link RouteAliasManager} that can map
 * abstract connector names to actual URIs.
 */
public class AliasedRoutesDefinition
    extends RoutesDefinition
{
    private RouteAliasManager aliasManager;

    AliasedRoutesDefinition( final RouteAliasManager aliasManager )
    {
        this.aliasManager = aliasManager;
    }

    protected RouteDefinition createRoute() {
        RouteDefinition route = new AliasedRouteDefinition( aliasManager );
        ErrorHandlerFactory handler = getErrorHandlerBuilder();
        if (handler != null) {
            route.setErrorHandlerBuilderIfNull(handler);
        }
        return route;
    }
}
