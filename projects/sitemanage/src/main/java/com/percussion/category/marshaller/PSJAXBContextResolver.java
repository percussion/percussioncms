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

package com.percussion.category.marshaller;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.category.data.PSDateAdapter;
import com.percussion.util.PSSiteManageBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBException;

@PSSiteManageBean("categoryContextResolver")
public class PSJAXBContextResolver implements ContextResolver<ObjectMapper> {

    private ObjectMapper objectMapper;
    private Class[] types = {PSCategory.class, PSCategoryNode.class, PSDateAdapter.class};
    private static final Logger log = LogManager.getLogger(PSJAXBContextResolver.class.getName());

    public PSJAXBContextResolver() throws JAXBException {
        //this.context =  new JSONJAXBContext(JSONConfiguration.natural().build(), types);
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true)
                .setAnnotationIntrospector(AnnotationIntrospector.pair(
                        new JacksonAnnotationIntrospector(),
                        new JaxbAnnotationIntrospector(TypeFactory.defaultInstance())));

	     /*
		 objectMapper.set
		    
		         new JAXBContext(Json
				 .mapped()
				 .arrays("topLevelNodes")
				 .arrays("childNodes")
				 .attributeAsElement("id","title","sitename","selectable","previousCategoryName","showInPgMetaData","initialViewCollapsed","createdBy","creationDate","lastModifiedBy","lastModifiedDate","publishDate","deleted")
				 .build(), types);
				 */
    }

    @Override
    public ObjectMapper getContext(Class<?> arg0) {
        for (Class type : types) {
            if (type == arg0) {
                log.debug("Check changes to PSJaxBContextResolver");
                return this.objectMapper;
            }
        }
        return null;
    }
}
