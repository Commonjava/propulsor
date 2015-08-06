package org.commonjava.propulsor.deploy.undertow.ui;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.propulsor.deploy.undertow.UndertowDeploymentProvider;

@Alternative
@Named
public class UIDeploymentProvider
    implements UndertowDeploymentProvider
{

    private UIServlet servlet;

    public UIDeploymentProvider( UIServlet servlet )
    {
        this.servlet = servlet;
    }

    @Override
    public DeploymentInfo getDeploymentInfo()
    {
        ServletInfo si = new ServletInfo( "ui-servlet", UIServlet.class ).setLoadOnStartup( 99 )
                                                                         .addMapping( "/*.html" )
                                                                         .addMapping( "/" )
                                                                         .addMapping( "/js/*" )
                                                                         .addMapping( "/css/*" )
                                                                         .addMapping( "/partials/*" )
                                                                         .addMapping( "/ui-addons/*" );

        si.setInstanceFactory( new ImmediateInstanceFactory<UIServlet>( servlet ) );

        return new DeploymentInfo().addServlet( si );
    }

}
