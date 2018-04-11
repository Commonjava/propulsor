/**
 * Copyright (C) 2015 John Casey (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.propulsor.deploy.undertow.ui;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.commonjava.propulsor.deploy.undertow.UndertowDeploymentProvider;

@ApplicationScoped
public class UIDeploymentProvider
    implements UndertowDeploymentProvider
{

    @Inject
    private UIServlet servlet;

    @Inject
    private Instance<UIConfiguration> config;

    public UIDeploymentProvider() {}

    public UIDeploymentProvider( UIServlet servlet )
    {
        this.servlet = servlet;
    }

    @Override
    public DeploymentInfo getDeploymentInfo()
    {
        if ( !config.isUnsatisfied() && config.get().isEnabled() )
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
        else
        {
            return new DeploymentInfo();
        }
    }

}
