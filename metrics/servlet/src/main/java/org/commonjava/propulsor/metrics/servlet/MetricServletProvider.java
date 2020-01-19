package org.commonjava.propulsor.metrics.servlet;

import io.undertow.servlet.api.DeploymentInfo;

public interface MetricServletProvider
{
    DeploymentInfo get();
}
