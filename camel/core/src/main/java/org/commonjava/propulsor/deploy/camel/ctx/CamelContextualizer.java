package org.commonjava.propulsor.deploy.camel.ctx;

import org.apache.camel.CamelContext;
import org.commonjava.propulsor.lifecycle.AppLifecycleException;

/**
 * Initialize components and other extensions to Camel. This initialization step takes place BEFORE routes are added.
 */
public interface CamelContextualizer
{
    void contextualize( CamelContext orCreateCamelContext )
            throws AppLifecycleException;
}
