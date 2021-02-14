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
import com.fasterxml.jackson.dataformat.xml.jaxb.XmlJaxbAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryFileLockData;
import com.percussion.server.PSServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	
	private static Log log = LogFactory.getLog(PSCategoryMarshaller.class);
	private PSCategory category;
	private static Map<String, PSCategoryFileLockData> lockMap = new HashMap<String, PSCategoryFileLockData>();

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
