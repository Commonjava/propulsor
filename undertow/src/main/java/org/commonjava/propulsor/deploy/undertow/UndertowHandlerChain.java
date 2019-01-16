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
