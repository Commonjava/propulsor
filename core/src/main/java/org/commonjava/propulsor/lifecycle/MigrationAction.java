/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.propulsor.lifecycle;

/**
 * Converts storage/data formats from older versions of AProx into up-to-date
 * forms. These are run right after the system boots, but before AProx enters
 * its startup sequence (see {@link StartupAction}).
 */
public interface MigrationAction extends AppLifecycleAction {

    /**
     * Execute the migration, and return whether anything was changed as a
     * result.
     */
    boolean migrate() throws AppLifecycleException;

}
