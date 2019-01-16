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
package org.commonjava.propulsor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.commonjava.propulsor.conf.KafkaPublisherConfig;
import org.commonjava.propulsor.content.audit.FileEventPublisher;
import org.commonjava.propulsor.content.audit.FileEventPublisherException;
import org.commonjava.propulsor.content.audit.model.FileEvent;
import org.commonjava.propulsor.content.audit.model.FileGroupingEvent;

import javax.inject.Inject;
import java.util.Properties;

public class KafkaPublisher
                implements FileEventPublisher
{

    private String topic;

    private Properties props;

    private ResultHandler handler;

    private Producer<String, String> producer;

    private ObjectMapper mapper;

    @Inject
    public KafkaPublisher( KafkaPublisherConfig config, ObjectMapper mapper, ResultHandler handler )
    {
        props = config.getConfiguration();
        topic = config.getTopic();

        this.mapper = mapper;
        this.handler = handler;
    }

    public void start() { producer = new KafkaProducer<>( props ); }

    @Override
    public void publishFileEvent( FileEvent fileEvent ) throws FileEventPublisherException
    {
        doPublish( fileEvent.getEventId().toString(), writeValueAsString( fileEvent ) );
    }

    @Override
    public void publishFileGroupingEvent( FileGroupingEvent fileGroupingEvent ) throws FileEventPublisherException
    {
        doPublish( fileGroupingEvent.getEventId().toString(), writeValueAsString( fileGroupingEvent ) );
    }

    private String writeValueAsString( Object value )
    {
        try
        {
            return mapper.writeValueAsString( value );
        }
        catch ( JsonProcessingException e )
        {
            throw new FileEventPublisherException( "Convert Object to JSON error.", e );
        }
    }

    private void doPublish( String key, String value )
    {

        ProducerRecord rp = new ProducerRecord<String, String>( topic, key, value );
        producer.send( rp, new KafkaPublisherCallback( key, value, handler ) );

    }

    public void close()
    {
        producer.close();
    }

}
