package org.commonjava.propulsor.camel.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.commonjava.propulsor.deploy.camel.ctx.CamelContextualizer;
import org.commonjava.propulsor.lifecycle.AppLifecycleException;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by jdcasey on 7/10/17.
 */
@ApplicationScoped
public class EmbeddedActiveMQLifecycleManager
        implements CamelContextualizer
{
    private ActiveMQConnectionFactory connectionFactory;

    @Override
    public void contextualize( final CamelContext context )
            throws AppLifecycleException
    {
        connectionFactory = new ActiveMQConnectionFactory( "vm://localhost" );
        context.addComponent( "jms", JmsComponent.jmsComponentAutoAcknowledge( connectionFactory ) );
    }
}
