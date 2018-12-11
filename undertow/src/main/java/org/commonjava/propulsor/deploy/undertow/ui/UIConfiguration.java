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
package org.commonjava.propulsor.deploy.undertow.ui;

import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;

@ApplicationScoped
@SectionName("ui")
public class UIConfiguration {

    public static final boolean DEFAULT_ENABLED = true;

    private Boolean enabled;

    private File uiDir;

    public UIConfiguration() {
    }

    public UIConfiguration(final File uiDir) {
        this.uiDir = uiDir;
    }

    public File getUIDir() {
        return uiDir;
    }

    @ConfigName( "ui.dir")
    public void setUIDir(final File uiDir) {
        this.uiDir = uiDir;
    }

    public Boolean isEnabled()
    {
        return enabled == null ? DEFAULT_ENABLED : enabled;
    }

    @ConfigName( "enabled" )
    public void setEnabled( Boolean enabled )
    {
        this.enabled = enabled;
    }
}
