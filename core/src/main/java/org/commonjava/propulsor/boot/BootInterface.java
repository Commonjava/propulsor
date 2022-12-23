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
package org.commonjava.propulsor.boot;

import org.commonjava.propulsor.config.ConfiguratorException;
import org.commonjava.propulsor.deploy.DeployException;
import org.commonjava.propulsor.lifecycle.AppLifecycleException;

/**
 * <p>Interface providing standardized methods and signals used to boot Indy.</p>
 *
 * <p>Boot sequence (contained within {@link #start(BootOptions)} method):</p>
 * <ol>
 * <li>{@link #initialize(BootOptions)}</li>
 * <li>{@link #config()}</li>
 * <li>{@link #startLifecycle()}</li>
 * <li>{@link #deploy()}</li>
 * </ol>
 *
 * <p>The {@link #runAndWait(BootOptions)} method calls {@link #start(BootOptions)} and then {@link #wait()} on the {@link BootInterface} instance
 * itself, such that {@link Thread#interrupt()} is necessary to make it return. This is the method normally used from the booter <tt>main()</tt>
 * method.</li>
 *
 * @author jdcasey
 */
public interface BootInterface
{
    void runAndWait( final BootOptions bootOptions ) throws BootException;

    void start( BootOptions bootOptions ) throws BootException;

    void stop();

    void initialize( final BootOptions bootOptions ) throws BootException;

    void config() throws ConfiguratorException;

    void startLifecycle() throws AppLifecycleException;

    void deploy() throws DeployException;

    BootOptions getBootOptions();

}
