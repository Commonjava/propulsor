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
package org.commonjava.propulsor.lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class AppLifecycleManager {

    private static final Comparator<AppLifecycleAction> PRIORITY_COMPARATOR =
            ( one, two ) -> new Integer( one.getPriority() ).compareTo( two.getPriority() );

    @Inject
    private Instance<BootupAction> bootupActionInstances;

    private List<BootupAction> bootupActions;

    @Inject
    private Instance<MigrationAction> migrationActionInstances;

    private List<MigrationAction> migrationActions;

    @Inject
    private Instance<StartupAction> startupActionInstances;

    private List<StartupAction> startupActions;

    @Inject
    private Instance<ShutdownAction> shutdownActionInstances;

    private List<ShutdownAction> shutdownActions;

    protected AppLifecycleManager() {
    }

    public AppLifecycleManager(final List<BootupAction> bootupActions, final List<MigrationAction> migrationActions, final List<StartupAction> startupActions, final List<ShutdownAction> shutdownActions) {
        this.bootupActions = bootupActions;
        this.migrationActions = migrationActions;
        this.startupActions = startupActions;
        this.shutdownActions = shutdownActions;
    }

    public void startup()
            throws AppLifecycleException
    {
        boot();
        migrate();
        start();
    }

    private void start()
            throws AppLifecycleException
    {
        Collections.sort( startupActions, PRIORITY_COMPARATOR );

        for ( final StartupAction action : startupActions )
        {
            action.start();
        }
    }

    private void migrate()
            throws AppLifecycleException
    {
        Collections.sort( migrationActions, PRIORITY_COMPARATOR );

        for ( final MigrationAction action : migrationActions )
        {
            action.migrate();
        }
    }

    private void boot()
            throws AppLifecycleException
    {
        Collections.sort( bootupActions, PRIORITY_COMPARATOR );

        for ( final BootupAction action : bootupActions )
        {
            action.init();
        }
    }

    public void stop() {
        Collections.sort( shutdownActions, PRIORITY_COMPARATOR );

        for (final ShutdownAction shutdownAction : shutdownActions) {
            shutdownAction.shutdown();
        }
    }

    @PostConstruct
    public void initCDI() {
        bootupActions = new ArrayList<>();
        for (final BootupAction action : bootupActionInstances) {
            bootupActions.add(action);
        }

        migrationActions = new ArrayList<>();
        for (final MigrationAction action : migrationActionInstances) {
            migrationActions.add( action );
        }

        startupActions = new ArrayList<>();
        for (final StartupAction action : startupActionInstances) {
            startupActions.add( action );
        }

        shutdownActions = new ArrayList<>();
        for (final ShutdownAction shutdownAction : shutdownActionInstances) {
            shutdownActions.add(shutdownAction);
        }
    }

    public void installShutdownHook() {
        Runtime.getRuntime()
               .addShutdownHook( new Thread( ()->stop() ) );
    }
}
