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
