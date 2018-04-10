package org.commonjava.propulsor.camel.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.commonjava.propulsor.deploy.camel.ctx.CamelContextualizer;
import org.commonjava.propulsor.lifecycle.AppLifecycleException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Initialize ActiveMQ connection factory for use in Camel-driven Propulsor apps.
 */
@ApplicationScoped
public class CamelActiveMQLifecycleManager
        implements CamelContextualizer
{
    private ActiveMQConnectionFactory connectionFactory;

    @Inject
    private CamelActiveMQConfig config;

    @Override
    public void contextualize( final CamelContext context )
            throws AppLifecycleException
    {
//        new ActiveMQSslConnectionFactory(  ).setKeyAndTrustManagers( ... );
        connectionFactory = new ActiveMQConnectionFactory( config.getBrokerUrl() );
        context.addComponent( "jms", JmsComponent.jmsComponentAutoAcknowledge( connectionFactory ) );
    }
}
