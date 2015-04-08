package org.commonjava.propulsor.deploy;

import org.commonjava.propulsor.boot.BootStatus;

public interface Deployer {

    BootStatus deploy() throws DeployerException;

    void stop();

}
