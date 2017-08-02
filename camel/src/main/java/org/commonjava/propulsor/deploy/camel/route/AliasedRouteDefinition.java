package org.commonjava.propulsor.deploy.camel.route;

import org.apache.camel.model.RouteDefinition;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrap {@link RouteDefinition} with support for looking up abstract route URI aliases using {@link RouteAliasManager}.
 */
public class AliasedRouteDefinition
    extends RouteDefinition
{
    private RouteAliasManager aliasManager;

    AliasedRouteDefinition( final RouteAliasManager aliasManager )
    {
        this.aliasManager = aliasManager;
    }

    public RouteDefinition fromAlias( final String uri )
    {
        return super.from( aliasManager.lookup( uri ) );
    }

    public RouteDefinition fromAlias( final String... uris )
    {
        return super.from( Stream.of( uris )
                                    .map( src -> aliasManager.lookup( src ) )
                                    .collect( Collectors.toList() )
                                    .toArray( new String[uris.length] ) );
    }

    public RouteDefinition toAlias( final String uri )
    {
        return super.to( aliasManager.lookup( uri ) );
    }

    public RouteDefinition toAlias( final String... uris )
    {
        return super.to( Stream.of( uris )
                                 .map( src -> aliasManager.lookup( src ) )
                                 .collect( Collectors.toList() )
                                 .toArray( new String[uris.length] ) );
    }

}
