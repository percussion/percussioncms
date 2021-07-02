/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
