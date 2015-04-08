package org.commonjava.propulsor.config;

import org.commonjava.propulsor.boot.BootOptions;

public interface Configurator {

    void load(BootOptions options) throws ConfiguratorException;

}
