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
 * Performs some sort of service/subsystem bootup just before AProx is preparing
 * to run.
 */
public interface BootupAction extends AppLifecycleAction {

    /**
     * Initialize some subsystem before migration actions start.
     */
    void init() throws AppLifecycleException;

}
