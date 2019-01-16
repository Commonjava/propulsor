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
package org.commonjava.propulsor.deploy.camel.ctx;

import org.apache.camel.CamelContext;
import org.commonjava.propulsor.lifecycle.AppLifecycleException;

/**
 * Initialize components and other extensions to Camel. This initialization step takes place BEFORE routes are added.
 */
public interface CamelContextualizer
{
    void contextualize( CamelContext orCreateCamelContext )
            throws AppLifecycleException;
}
