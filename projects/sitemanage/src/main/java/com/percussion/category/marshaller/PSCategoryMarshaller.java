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
import com.fasterxml.jackson.dataformat.xml.jaxb.XmlJaxbAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryFileLockData;
import com.percussion.server.PSServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashMap;
import java.util.Map;

@Component("categoryMarshaller")
@Lazy
public class PSCategoryMarshaller {
	
	private static final Logger log = LogManager.getLogger(PSCategoryMarshaller.class);
	private PSCategory category;
	private static Map<String, PSCategoryFileLockData> lockMap = new HashMap<>();

	public PSCategory getCategory() {
		return category;
	}

	public void setCategory(PSCategory category) {
		this.category = category;
	}
	
	public void marshal() throws OverlappingFileLockException {
		
		File file = new File(PSServer.getRxDir(), "rx_resources/category/category.xml");
		
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("The 'category.xml' file does not exist. There was a exception while creating a new file.", e);
			}
		}
		
		FileOutputStream fos = null;
		FileChannel channel = null;
		FileLock lock = null;
		
		try {

		    JAXBContext jaxbContext = JAXBContext.newInstance(PSCategory.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			
			fos = new FileOutputStream(file);
			channel = fos.getChannel();
			lock = channel.tryLock();
			
			
			if(lock == null) {
				throw new IllegalArgumentException("File is locked by another user. Please try later.");
			}
	
			lockMap.put("category.xml", new PSCategoryFileLockData(lock, LocalDateTime.now()));
			log.debug("Lock acquired on the category xml file !!");
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(category, fos);
		} catch (IOException e) {
			throw new RuntimeException("Error trying to write to category file "+file.getPath(),e);
		} catch (JAXBException e) {
		    throw new RuntimeException("Error writing category object to file "+file.getPath(),e);
		} finally {
			
			try {
				//FB: NP_ALWAYS_NULL_EXCEPTION NC 1=16-16
				if(lock!=null){
					lock.release();
				}
				lockMap.remove("category.xml");
				log.debug("Lock on the category xml file is released successfully !!");
		
				//FB: NP_ALWAYS_NULL_EXCEPTION NC 1=16-16
				if(fos !=null){
					fos.close();
				}
			} catch (IOException e) {
			    throw new RuntimeException("Cannot release file lock on category file "+file.getPath(),e);
			}
		}
	}
	
	public static String marshalToJson(PSCategory category) {

	    StringWriter writer = new StringWriter();
		try {
		    ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		    AnnotationIntrospector introspector = new XmlJaxbAnnotationIntrospector(mapper.getTypeFactory());
		    mapper.getSerializationConfig().withAppendedAnnotationIntrospector(introspector);
		
		    mapper.writeValue(writer, category);
		} catch ( IOException e) {
		    log.debug("Cannot convert category object to json string",e);
		    throw new RuntimeException("Error processing category data",e);
		
		}

		return writer.toString();
	}
	
	public boolean releaseLock() {
	    
		String fileName = "category.xml";
		
		PSCategoryFileLockData lockData = lockMap.get(fileName);
		
		if(lockData != null) {
			FileLock lock = lockData.getLock();
			
			try {
				
				// Logic can be added to check the time before removing the lock.
				lock.release();
				
				lockMap.remove(fileName);
				
				return true;
			} catch (IOException e) {
			    throw new RuntimeException("Cannot release file lock on category file ",e);
			}
		}
		
		return false;
	}
}
