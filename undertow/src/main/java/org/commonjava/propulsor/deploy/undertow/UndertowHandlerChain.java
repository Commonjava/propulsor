package org.commonjava.propulsor.deploy.undertow;

import io.undertow.server.HttpHandler;

/**
 * Customization point that allows you to wrap the base {@link HttpHandler} from the
 * {@link io.undertow.servlet.api.DeploymentManager} instance (created from the
 * {@link io.undertow.servlet.api.DeploymentInfo}, which in turn is constructed from
 * {@link UndertowDeploymentProvider} instances provided by your application.
 */
public interface UndertowHandlerChain
{
    HttpHandler getHandler( HttpHandler baseHandler );
}
