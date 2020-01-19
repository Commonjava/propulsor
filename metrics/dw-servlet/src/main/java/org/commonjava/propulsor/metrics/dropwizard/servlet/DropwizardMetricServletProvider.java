package org.commonjava.propulsor.metrics.dropwizard.servlet;

import com.codahale.metrics.servlets.HealthCheckServlet;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import org.commonjava.propulsor.metrics.servlet.MetricServletProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
@Named
public class DropwizardMetricServletProvider
        implements MetricServletProvider
{

    private final DropwizardServletConfig config;

    @Inject
    public DropwizardMetricServletProvider( DropwizardServletConfig config )
    {

        this.config = config;
    }

    @Override
    public DeploymentInfo get()
    {
        return new DeploymentInfo().addListener(
                Servlets.listener( DropwizardHealthCheckServletContextListener.class ) )
                                   .setContextPath( config.getContextPath() )
                                   .addServlet( Servlets.servlet( "healthcheck", HealthCheckServlet.class )
                                                        .addMapping( config.getContextPath() ) )
                                   .setDeploymentName( "Dropwizard HealthCheck Deployment" )
                                   .setClassLoader( ClassLoader.getSystemClassLoader() );
    }
}
