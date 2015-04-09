package org.commonjava.propulsor.lifecycle;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class AppLifecycleManager {

    @Inject
    private Instance<ShutdownAction> shutdownActionInstances;

    private List<ShutdownAction> shutdownActions;

    protected AppLifecycleManager() {
    }

    public AppLifecycleManager(final List<ShutdownAction> shutdownActions) {
        this.shutdownActions = shutdownActions;
    }

    public void stop() {
        for (final ShutdownAction shutdownAction : shutdownActions) {
            shutdownAction.shutdown();
        }
    }

    @PostConstruct
    public void initCDI() {
        shutdownActions = new ArrayList<>();
        for (final ShutdownAction shutdownAction : shutdownActionInstances) {
            shutdownActions.add(shutdownAction);
        }
    }

    public void installShutdownHook() {
        Runtime.getRuntime()
               .addShutdownHook( new Thread( new ShutdownRunnable() ) );
    }

    private final class ShutdownRunnable
        implements Runnable
    {
        @Override
        public void run()
        {
            stop();
        }
    }
}
