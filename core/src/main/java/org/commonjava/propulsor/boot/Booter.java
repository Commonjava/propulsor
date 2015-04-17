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
package org.commonjava.propulsor.boot;

import static org.commonjava.propulsor.boot.BootStatus.ERR_LOAD_CONFIG;
import static org.commonjava.propulsor.boot.BootStatus.ERR_LOAD_FROM_SYSPROPS;
import static org.commonjava.propulsor.boot.BootStatus.ERR_PARSE_ARGS;
import static org.commonjava.propulsor.boot.BootStatus.ERR_STARTING;

import java.io.File;
import java.io.IOException;
import java.util.ServiceLoader;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.commonjava.propulsor.config.Configurator;
import org.commonjava.propulsor.config.ConfiguratorException;
import org.commonjava.propulsor.deploy.Deployer;
import org.commonjava.propulsor.lifecycle.AppLifecycleManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Booter {

    private static final String BOOT_DEFAULTS_PROP = "boot.properties";

    public static void main(final String[] args) {
        BootOptions options = null;
        try {
            options = loadBootOptions();
        } catch (final BootException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(ERR_LOAD_FROM_SYSPROPS);
        }

        try {
            options.parseArgs(args);
        } catch (final BootException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(ERR_PARSE_ARGS);
        }

        BootStatus status = null;
        try {
            status = new Booter().runAndWait(options);
        } catch (final BootException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(ERR_STARTING);
        }

        if (status.isFailed()) {
            status.getError().printStackTrace();
            System.err.println(status.getError().getMessage());
            System.exit(status.getExitCode());
        }
    }

    private static BootOptions loadBootOptions() throws BootException {
        final String bootDef = System.getProperty(BOOT_DEFAULTS_PROP);
        File bootDefaults = null;
        if (bootDef != null) {
            bootDefaults = new File(bootDef);
        }

        final ServiceLoader<BootOptions> loader = ServiceLoader
                .load(BootOptions.class);
        final BootOptions options = loader.iterator().next();

        try {
            String home = System.getProperty(options.getHomeSystemProperty());

            if (home == null) {
                home = System.getenv(options.getHomeEnvar());
            }

            if (home == null) {
                home = new File(".").getCanonicalPath();
            }

            options.load(bootDefaults, home);
            return options;
        } catch (final IOException e) {
            throw new BootException(
                    "ERROR LOADING BOOT DEFAULTS: %s.\nReason: %s\n\n", e,
                    bootDefaults, e.getMessage());
        } catch (final InterpolationException e) {
            throw new BootException(
                    "ERROR RESOLVING BOOT DEFAULTS: %s.\nReason: %s\n\n", e,
                    bootDefaults, e.getMessage());
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private BootOptions options;

    private Weld weld;

    private WeldContainer container;

    private Deployer deployer;

    private Configurator configurator;

    private BootStatus status;

    private AppLifecycleManager lifecycleManager;

    private void initialize(final BootOptions options) throws BootException {
        this.options = options;

        try {
            options.setSystemProperties();

            weld = new Weld();
            container = weld.initialize();
        } catch (final RuntimeException e) {
            throw new BootException("Failed to initialize Booter: "
                    + e.getMessage(), e);
        }
    }

    public BootStatus runAndWait(final BootOptions bootOptions)
            throws BootException {
        start(bootOptions);

        logger.info("Setting up shutdown hook...");
        lifecycleManager.installShutdownHook();

        synchronized (deployer) {
            try {
                deployer.wait();
            } catch (final InterruptedException e) {
                e.printStackTrace();
                logger.info("AProx exiting");
            }
        }

        return status;
    }

    public WeldContainer getContainer() {
        return container;
    }

    public BootOptions getBootOptions() {
        return options;
    }

    public boolean deploy() {
        deployer = container.instance().select(Deployer.class).get();
        status = deployer.deploy( options );

        return status == null ? false : status.isSuccess();
    }

    public BootStatus start(final BootOptions bootOptions) throws BootException {
        initialize(bootOptions);
        logger.info("Booter running: " + this);

        configure();
        startLifecycle();

        deploy();
        return status;
    }

    public void configure() {
        configurator = container.instance().select(Configurator.class).get();
        try {
            configurator.load(options);
        } catch (final ConfiguratorException e) {
            status.markFailed(ERR_LOAD_CONFIG, e);
        }
    }

    public void startLifecycle() {
        lifecycleManager = container.instance()
                .select(AppLifecycleManager.class).get();
    }

    public void stop() {
        if (container != null) {
            deployer.stop();
            lifecycleManager.stop();
            weld.shutdown();
        }
    }
}
