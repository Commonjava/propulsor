package org.commonjava.propulsor.metrics.dropwizard.config;

import org.commonjava.propulsor.config.annotation.SectionName;
import org.commonjava.propulsor.metrics.conf.MetricSubsetConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@ApplicationScoped
@Named
@SectionName("metrics.dropwizard")
public class DropwizardConfig
        extends MetricSubsetConfig<DropwizardConfig>
{

}
