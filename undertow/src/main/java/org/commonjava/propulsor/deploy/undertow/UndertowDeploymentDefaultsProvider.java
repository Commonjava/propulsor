package org.commonjava.propulsor.deploy.undertow;

import io.undertow.servlet.api.DeploymentInfo;

public interface UndertowDeploymentDefaultsProvider
{

    void setDefaults( DeploymentInfo di );

}
