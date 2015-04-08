package org.commonjava.propulsor.lifecycle;

public interface AppLifecycleAction {

    /** Used mainly for reporting, this is a unique identifier for this action. */
    String getId();

    /**
     * Used to sort the actions, with highest priority executing first. Priority
     * should generally be between 1-100.
     */
    int getPriority();

}
