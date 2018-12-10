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
package org.commonjava.propulsor.content.audit.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class FileEvent
{

    private UUID eventId;

    private String sessionId;

    private String nodeId;

    private String checksum;

    private String targetLocation;

    private String targetPath;

    private String eventType;

    private String requestId;

    private Integer eventVersion;

    private Map<String, String> extra;

    @JsonFormat( shape = JsonFormat.Shape.STRING )
    private Date timestamp;

    public FileEvent( String eventType )
    {
        this.eventId = UUID.randomUUID();
        this.eventVersion = 1;
        this.eventType = eventType;
    }

    public UUID getEventId() { return eventId; }

    public String getSessionId() { return sessionId; }

    public void setSessionId( String sessionId ) { this.sessionId = sessionId; }

    public String getNodeId() { return nodeId; }

    public void setNodeId( String nodeId ) { this.nodeId = nodeId; }

    public String getChecksum() { return checksum; }

    public void setChecksum( String checksum ) { this.checksum = checksum; }

    public String getTargetLocation() { return targetLocation; }

    public void setTargetLocation( String targetLocation ) { this.targetLocation = targetLocation; }

    public String getTargetPath() { return targetPath; }

    public void setTargetPath( String targetPath ) { this.targetPath = targetPath; }

    public String getEventType() { return eventType; }

    public void setEventType( String eventType ) { this.eventType = eventType; }

    public String getRequestId() { return requestId; }

    public void setRequestId( String requestId ) { this.requestId = requestId; }

    public Integer getEventVersion() { return eventVersion; }

    public void setEventVersion( Integer eventVersion ) { this.eventVersion = eventVersion; }

    public Date getTimestamp() { return timestamp; }

    public void setTimestamp( Date timestamp ) { this.timestamp = timestamp; }

    public Map<String, String> getExtra() { return extra; }

    public void setExtra( Map<String, String> extra ) { this.extra = extra; }

}
