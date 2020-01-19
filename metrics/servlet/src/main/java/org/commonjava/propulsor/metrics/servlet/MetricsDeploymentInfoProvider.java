package org.commonjava.propulsor.metrics.servlet;

import io.undertow.servlet.api.DeploymentInfo;
import org.commonjava.propulsor.deploy.undertow.UndertowDeploymentProvider;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.commonjava.propulsor.deploy.undertow.util.DeploymentInfoUtils.merge;

public class MetricsDeploymentInfoProvider
        implements UndertowDeploymentProvider
{
    private final Set<DeploymentInfo> metricDeploymentInfos;

    @Inject
    public MetricsDeploymentInfoProvider( Instance<MetricServletProvider> metricServletProviders )
    {
        Set<DeploymentInfo> providerSet = new HashSet<>();
        metricServletProviders.forEach( msp -> providerSet.add( msp.get() ) );

        this.metricDeploymentInfos = providerSet;
    }

    public MetricsDeploymentInfoProvider( Set<MetricServletProvider> metricServletProviders )
    {
        Set<DeploymentInfo> providerSet = new HashSet<>();
        metricServletProviders.forEach( msp -> providerSet.add( msp.get() ) );

        this.metricDeploymentInfos = providerSet;
    }

    public MetricsDeploymentInfoProvider( DeploymentInfo metricServletDeployment )
    {
        this.metricDeploymentInfos = Collections.singleton( metricServletDeployment );
    }

    public DeploymentInfo getDeploymentInfo()
    {
        DeploymentInfo di = new DeploymentInfo();

        merge( di, metricDeploymentInfos );
        return di;
    }
}
