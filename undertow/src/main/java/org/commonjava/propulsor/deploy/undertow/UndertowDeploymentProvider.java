/**
 * Copyright (C) 2014-2022 Red Hat, Inc. (http://github.com/Commonjava/commonjava)
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

import io.undertow.servlet.api.DeploymentInfo;

/**
 * Specifies the information normally configured in the web.xml file for a WAR file. This is where you configure
 * servlets, filters, etc.
 */
public interface UndertowDeploymentProvider
{

    DeploymentInfo getDeploymentInfo();

}
