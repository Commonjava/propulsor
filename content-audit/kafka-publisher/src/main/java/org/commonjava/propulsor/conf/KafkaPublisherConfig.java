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
package org.commonjava.propulsor.conf;

import org.commonjava.propulsor.config.annotation.SectionName;
import org.commonjava.propulsor.config.section.PropertiesSectionListener;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@SectionName("publisher.kafka")
public class KafkaPublisherConfig extends PropertiesSectionListener
{

    private String topic;

    public String getTopic() { return topic; }

    public void setTopic( String topic ) { this.topic = topic; }

}
