/**
 * Copyright (C) 2011-2018 Red Hat, Inc. (https://github.com/Commonjava/propulsor)
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
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.commonjava.propulsor.content.audit.FileEventPublisher;
import org.commonjava.propulsor.content.audit.FileEventPublisherException;
import org.commonjava.propulsor.content.audit.model.FileEvent;
import org.commonjava.propulsor.content.audit.model.FileGroupingEvent;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class KafkaPublisher
                implements FileEventPublisher
{

    private static final String PROP_PATH = "publisher.properties";

    private String topic;

    private Properties props;

    private ResultHandler handler;

    private Producer<String, String> producer;

    public KafkaPublisher( String bootStrapServers, String topic, ResultHandler handler )
    {
        this.topic = topic;
        this.handler = handler;

        initProperties( bootStrapServers );

        producer = new KafkaProducer<>( props );
    }

    private void initProperties( String bootStrapServers )
    {
        props = new Properties();

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( new File( PROP_PATH ) );
            props.load( fis );
        }
        catch ( Exception e )
        {
            throw new FileEventPublisherException( "Load configuration failed.", e );
        }
        finally
        {
            IOUtils.closeQuietly( fis );
        }

        props.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers );
    }

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
            return new ObjectMapper().writeValueAsString( value );
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
