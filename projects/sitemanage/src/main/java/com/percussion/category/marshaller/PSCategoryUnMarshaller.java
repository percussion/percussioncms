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
import com.fasterxml.jackson.dataformat.xml.jaxb.XmlJaxbAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.category.transformer.PSCategoryXmlTransform;
import com.percussion.server.PSServer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("categoryUnmarshaller")
@Lazy
public class PSCategoryUnMarshaller {
	
	private static final String LEGACY_ADD_TOP_LEVEL_CATEGORIES = "Add Top Level Categories";
    private static final Logger log = LogManager.getLogger(PSCategoryUnMarshaller.class);
	
	public PSCategory unMarshal() {
		
		PSCategory category = null;
		File file = createCategoryFileIfNotExisting();

		if(file == null)
			category = getEmptyCategory();

		if(category == null) {
		    JAXBContext jaxbContext;
			try {

				jaxbContext = JAXBContext.newInstance(PSCategory.class);

				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

				category = (PSCategory) jaxbUnmarshaller.unmarshal(file);

		        removeTopLevelNode(category);

			} catch (JAXBException e) {
				throw new RuntimeException("Invalid category.xml file "+file.getPath(),e);
			}
		}

		return category;
	}
	
	private void removeTopLevelNode(PSCategory category)
    {
	    ArrayList<PSCategoryNode> nodes = new ArrayList<>();
	    
	    for (PSCategoryNode node : category.getTopLevelNodes())
	    {
	       
	        if (!StringUtils.equals(node.getTitle(), LEGACY_ADD_TOP_LEVEL_CATEGORIES))
	                nodes.add(node);
	        else if (log.isDebugEnabled())
	            log.debug("Removing old "+ LEGACY_ADD_TOP_LEVEL_CATEGORIES +" category ");
	    }
        category.setTopLevelNodes(nodes);
    }

    public static File createCategoryFileIfNotExisting() {
		
	    File file = new File(PSServer.getRxDir(), "rx_resources/category/category.xml");
		
		if(!file.exists()) {
			File fromFile = new File(PSServer.getRxDir(), "/web_resources/categories/tree.xml");
			
			if(!fromFile.exists()) {

				PSCategoryMarshaller marshaller = new PSCategoryMarshaller();

				marshaller.setCategory(PSCategoryUnMarshaller.getEmptyCategory());
				marshaller.marshal();
				
			} else {
			    log.info("Transforming old categories tree.xml to new category.xml");
				// Transform the old format category xml to the new format.
				PSCategoryXmlTransform transformer = new PSCategoryXmlTransform();
				transformer.transformXml(fromFile, file);
			}
		}
		return file;
	}
	
	public static PSCategory getEmptyCategory() {
		// If none of the files exist then create an empty structure for a new category xml that will be created using the category editor.
		PSCategory category = new PSCategory();
		
		category.setTopLevelNodes(new ArrayList<>());
		
		return category;
	}
	
	public static PSCategory unMarshalFromString(String categoryJson) {
	    Reader reader = new StringReader(categoryJson);
	    PSCategory category = null;
        try {
            ObjectMapper mapper = new  ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            AnnotationIntrospector introspector = new XmlJaxbAnnotationIntrospector(mapper.getTypeFactory());
            mapper.getDeserializationConfig().withAppendedAnnotationIntrospector(
                    introspector);
          
            category = mapper.readValue(categoryJson, PSCategory.class);
        }
        catch (IOException e)
        {
            log.error("Error parsing category json: "+categoryJson,e);
            throw new RuntimeException("Unexpected error processing categories",e);
        }  finally {
            IOUtils.closeQuietly(reader);
        }
		return category;
	}
}
