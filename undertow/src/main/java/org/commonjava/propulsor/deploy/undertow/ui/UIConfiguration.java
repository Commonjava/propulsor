package org.commonjava.propulsor.deploy.undertow.ui;

import java.io.File;

import org.commonjava.web.config.annotation.ConfigNames;
import org.commonjava.web.config.annotation.SectionName;

@SectionName("ui")
public class UIConfiguration {
    private File uiDir;

    public UIConfiguration() {
    }

    @ConfigNames("ui.dir")
    public UIConfiguration(final File uiDir) {
        this.uiDir = uiDir;
    }

    public File getUIDir() {
        return uiDir;
    }

    public void setUIDir(final File uiDir) {
        this.uiDir = uiDir;
    }

}
