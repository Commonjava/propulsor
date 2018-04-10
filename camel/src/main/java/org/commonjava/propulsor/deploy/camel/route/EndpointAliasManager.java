package org.commonjava.propulsor.deploy.camel.route;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains aliases from keys that can be used within route definitions, to actual endpoint URIs that define a protocol
 * for the endpoint. This provides the opportunity to change deployment topologies for the same application by simply
 * changing the endpoint aliases.
 */
@ApplicationScoped
public class EndpointAliasManager
{
    private Map<String, String> routeMap = new HashMap<>();

    public synchronized void loadAliasMap( Map<String, String> aliases )
    {
        this.routeMap = new HashMap<>( aliases );
    }

    public String lookup( String key )
    {
        return routeMap.computeIfAbsent( key, k->k );
    }
}
