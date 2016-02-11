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
package org.commonjava.propulsor.lifecycle;

/**
 * Performs some sort of service/subsystem startup as application is preparing to run.
 */
public interface StartupAction extends AppLifecycleAction {

    /**
     * Start some service after migration actions are completed, just before
     * application is ready for use.
     */
    void start() throws AppLifecycleException;

}
