/**
 * Copyright (C) 2014-2022 Red Hat, Inc. (http://github.com/Commonjava/commonjava)
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
package org.commonjava.propulsor.deploy.resteasy.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.commons.lang.StringUtils;
import org.commonjava.propulsor.deploy.resteasy.RestProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Consumes( { "application/json", "application/*+json", "text/json" } )
@Produces( { "application/json", "application/*+json", "text/json" } )
//@ApplicationScoped
public class CDIJacksonProvider
    extends JacksonJsonProvider
    implements RestProvider
{

//    @Inject
//    private ObjectMapper mapper;
//
//    @Inject
//    private BeanManager bmgr;

    @Override
    public ObjectMapper locateMapper( final Class<?> type, final MediaType mediaType )
    {
        final CDI<Object> cdi = CDI.current();
        final ObjectMapper mapper = cdi.select( ObjectMapper.class ).get();

        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.debug( "Using ObjectMapper in Resteasy: {}", mapper );

        return mapper;
    }

    @Override
    public boolean isReadable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType )
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.debug( "Check isReadable() for: {}, {}, {}, {}", type, genericType,
                      "[" + StringUtils.join( annotations, ", " ) + "]", mediaType );

        return super.isReadable( type, genericType, annotations, mediaType );
    }

    @Override
    public boolean isWriteable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType )
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.debug( "Check isWritable() for: {}, {}, {}, {}", type, genericType,
                      "[" + StringUtils.join( annotations, ", " ) + "]", mediaType );

        return super.isWriteable( type, genericType, annotations, mediaType );
    }
}
