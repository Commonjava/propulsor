package org.commonjava.propulsor.deploy;

import org.commonjava.propulsor.boot.BootOptions;
import org.commonjava.propulsor.boot.BootStatus;

public interface Deployer {

    BootStatus deploy( BootOptions options );

    void stop();

}
