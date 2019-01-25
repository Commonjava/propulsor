package org.commonjava.propulsor.boot;

import org.jboss.weld.environment.se.WeldContainer;

public interface WeldBootInterface
                extends BootInterface
{

    WeldContainer getContainer();

}