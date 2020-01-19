package org.commonjava.propulsor.metrics.spi;

import java.util.Set;

public interface TimingContext
{
    void start();

    Set<Long> stop();
}
