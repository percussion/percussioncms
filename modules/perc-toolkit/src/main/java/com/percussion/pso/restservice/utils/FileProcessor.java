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
package com.percussion.pso.restservice.utils;

import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSBinaryFileValue;
import com.percussion.error.PSExceptionUtils;
import com.percussion.util.PSPurgableTempFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileProcessor {
	 /**
	    * Logger for this class
	    */
	   private static final Logger log = LogManager.getLogger(FileProcessor.class);
	
	public static  void process(PSCoreItem psItem) {
		log.debug("Processing Files");
		Iterator<PSItemField> iterator = psItem.getAllFields();
		while (iterator.hasNext()) {
			PSItemField field = iterator.next();
			log.debug("testing field "+field.getName());
			if (field.getItemFieldMeta().isBinary()) {
				log.debug("Field is binary");
				String name = field.getName();
				PSBinaryFileValue value = (PSBinaryFileValue)field.getValue();
				PSPurgableTempFile temp = value.getTempFile();
				 String mimetype = determineMimeType(
		                  temp.getSourceContentType(),
		                  temp.getSourceFileName());
				 log.debug("Mimetype is "+mimetype);
				 
				 PSItemField typeField = psItem.getFieldByName(name+"_type");
				
				String sourceName =   temp.getSourceFileName();
				 String encoding = temp.getCharacterSetEncoding();
				 int pos = temp.getSourceFileName().lastIndexOf(".");
	               if ( pos >= 0 )
	               {
	            	   setFieldValue(psItem,name+"_ext",sourceName.substring( pos ));
	               }
				 setFieldValue(psItem,name+"_type",mimetype);
		
				 setFieldValue(psItem,name+"_encoding",encoding);
				 long filesize = temp.length();
	             setFieldValue(psItem,name+"_size",String.valueOf(filesize));
				 setFieldValue(psItem,name+"_filename",new File(temp.getSourceFileName()).getName());	
				
				 
				 if (mimetype.contains("image") && filesize > 0)
	               {
						  ImageIO.scanForPlugins();
						  try {
							  BufferedImage image = ImageIO.read(temp.getCanonicalFile());

							  if (image != null)
							  {
								  setFieldValue(psItem,name+"_height",String.valueOf(image.getHeight()));
								  setFieldValue(psItem,name+"_width",String.valueOf(image.getWidth()));

							  }
						  } catch (IOException e) {
							  log.error(PSExceptionUtils.getMessageForLog(e));
							  log.debug(PSExceptionUtils.getDebugMessageForLog(e));
						  }
	               } //end if mimetype.indexOf...
	              
				 
				 
			}
			
		}
		
	}
	
	private static void setFieldValue(PSCoreItem psItem, String name,String value) {
		PSItemField typeField = psItem.getFieldByName(name);
		if (typeField!=null) {
		 	typeField.addValue(new PSTextValue(value));
		}
	}
	 /**
	    * This method tries to make a more intelligent decision to determine
	    * the appropriate Mime type by looking at both the type guess made
	    * by the browser and the file extension. Some browser do not always 
	    * correctly determine an uploaded files Mime type for HTML files. If the
	    * type guessed by the browser is text or octet-stream and the file extension
	    * is one of the well known extensions then we use that extensions 
	    * Mime type.
	    * 
	    * @param type the Mime type guessed by the browser, cannot be <code>
	    * null</code> or empty.
	    * @param filename the filename for the uploaded file, cannot be
	    * <code>null</code> or empty.
	    * @return the Mime type, never <code>null</code> or empty.
	    */
	   private static String determineMimeType(String type, String filename)
	   {
	      if(type == null || type.trim().length() == 0)
	         throw new IllegalArgumentException("The type cannot be null or empty.");
	      if(filename == null || filename.trim().length() == 0)
	         throw new IllegalArgumentException("The filename cannot be null or empty.");
	                  
	      int pos = filename.lastIndexOf('.');
	      String ext = pos == -1 ? "" : filename.substring(pos + 1).toLowerCase();
	      
	      if(ms_wellKnownExts.containsKey(ext)
	         && (type.toLowerCase().equals("application/octet-stream")
	            || type.toLowerCase().equals("text/plain")))
	       {
	           return ms_wellKnownExts.get(ext);  
	       }
	      return type;
	   }

	   /**
	    * These are a map of well know file extensions that the browser should not guess
	    * as either text or octet-stream Mime types, but some browser like IE can make
	    * a mistake with these. This list should be expanded as we find other problem
	    * extension types.
	    */
	   private static final Map<String, String> ms_wellKnownExts =
	         new HashMap<String, String>(2);   
	   static
	   {
	      ms_wellKnownExts.put("htm", "text/html");
	      ms_wellKnownExts.put("html", "text/html");     
	   }
	
	  
}
