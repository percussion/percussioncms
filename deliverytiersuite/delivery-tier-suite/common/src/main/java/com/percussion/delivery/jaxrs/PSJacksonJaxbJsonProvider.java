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

package com.percussion.delivery.jaxrs;

import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Provider  
public class PSJacksonJaxbJsonProvider extends JacksonJaxbJsonProvider
{
    // We Need to override org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper and org.codehaus.jackson.jaxrs.JsonParseExceptionMapper
    // These are included in the package we scan from com.sun.jersey.config.property.packages in web.xml
    // We still want the JacksonJaxbJsonProvider so we just extend it and find it here.  we have some more options when we upgrade
    // Jax-rs from 1.1 to 2.0
    // See discussion here https://github.com/fasterxml/jackson-jaxrs-providers/issues/22
    
}
