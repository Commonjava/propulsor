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

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaPublisherCallback implements Callback
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private String key;

    private String value;

    private ResultHandler handler;

    public KafkaPublisherCallback( String key, String value, ResultHandler handler )
    {
        this.key = key;
        this.value = value;
        this.handler = handler;
    }

    @Override
    public void onCompletion( RecordMetadata recordMetadata, Exception e )
    {
        if ( handler != null )
        {
            handler.handle( key, value, e );
        }
        else
        {
            logger.warn( "There is no default handler setting." );
        }
    }
}
