package org.commonjava.propulsor.deploy.undertow;

import io.undertow.servlet.api.DeploymentInfo;

public interface UndertowDeploymentProvider
{

    DeploymentInfo getDeploymentInfo();

}
