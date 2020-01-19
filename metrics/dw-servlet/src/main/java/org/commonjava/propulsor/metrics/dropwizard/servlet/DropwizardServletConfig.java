package org.commonjava.propulsor.metrics.dropwizard.servlet;

import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;

import javax.enterprise.context.ApplicationScoped;

@SectionName("metrics.dropwizard.servlet")
@ApplicationScoped
public class DropwizardServletConfig
{
    private static final String DEFAULT_CONTEXT_PATH = "/healthchecks";

    private String contextPath;

    public String getContextPath()
    {
        return contextPath == null ? DEFAULT_CONTEXT_PATH : contextPath;
    }

    @ConfigName( "context.path" )
    public void setContextPath( final String contextPath )
    {
        this.contextPath = contextPath;
    }
}
