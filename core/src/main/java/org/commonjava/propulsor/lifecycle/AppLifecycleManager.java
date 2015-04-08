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

    public AppLifecycleManager(List<ShutdownAction> shutdownActions) {
        this.shutdownActions = shutdownActions;
    }

    public void stop() {
        for (ShutdownAction shutdownAction : shutdownActions) {
            shutdownAction.shutdown();
        }
    }

    @PostConstruct
    public void initCDI() {
        shutdownActions = new ArrayList<>();
        for (ShutdownAction shutdownAction : shutdownActionInstances) {
            shutdownActions.add(shutdownAction);
        }
    }

    public void installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
        }));
    }
}
