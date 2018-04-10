package org.commonjava.propulsor.camel.activemq;

import javax.enterprise.context.ApplicationScoped;

/**
 * Configure the ActiveMQ connection factory for Camel.
 */
@ApplicationScoped
public class CamelActiveMQConfig
{
    public static final String DEFAULT_BROKER_URL = "vm://localhost";

    private String brokerUrl;

    public String getBrokerUrl()
    {
        return brokerUrl == null ? DEFAULT_BROKER_URL : brokerUrl;
    }

    public void setBrokerUrl( final String brokerUrl )
    {
        this.brokerUrl = brokerUrl;
    }
}
