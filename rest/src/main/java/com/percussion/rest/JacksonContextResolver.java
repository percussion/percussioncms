/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * @author stephenbolton
 *
 *  This is picked up by Jackson automatically by the Provider annotation
 *  It will modify the serialization behavior of the objects passed in
 *  we test that the class has the same ancestor package as this class
 *  to ensure we do not modify behavior for other parts of the system
 *
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonContextResolver implements ContextResolver<ObjectMapper>
{
    private static ObjectMapper objectMapper  = new ObjectMapper();
    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        // Indent may help with testing but can slow performance and can 
        // fail unit tests.
        // .configure(SerializationConfig.Feature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        false)
                .configure(SerializationFeature.WRAP_ROOT_VALUE,true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true)
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE,true);
    }

    @Override
    public ObjectMapper getContext(Class<?> objectType)
    {
        // only use this configuration for classes in same package and subpackages
        return (objectType.getPackage().getName().startsWith(JacksonContextResolver.class.getPackage().getName()))
                ? objectMapper
                : null;
    }
}
