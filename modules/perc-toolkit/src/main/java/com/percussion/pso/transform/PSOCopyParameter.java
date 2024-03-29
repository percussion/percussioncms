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
package com.percussion.pso.transform;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.utils.PathCleanupUtils;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

/***
 * A field input transform for copying one field to another
 * This is an extended version of the sys_copyParameter extension
 * that allows conditional copy.  This way you can copy an uploaded
 * filename to one used in the location field only if one has not been 
 * manually entered.  This can also concatenate the value if too large
 * for fields where the destination is smaller than the source.
 * 
 * @author stephenbolton
 *
 */
public class PSOCopyParameter implements IPSItemInputTransformer,IPSRequestPreProcessor
{
	
	private static final Logger log = LogManager.getLogger(PSOCopyParameter.class);
	 
   public PSOCopyParameter()
   {
      // nothing to do
   }

   
   // see IPSRequestPreProcessor
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
	   String emsg = "";
	   if (params == null || params.length < 4) {
		   throw new PSParameterMismatchException("Required Parameters are missing");
	   }

      // expects two string parameters   
      String sourceName = getParameter(params, 0);
      if(StringUtils.isBlank(sourceName))
      {
         emsg = "Source Field is required"; 
         log.error(emsg); 
         throw new IllegalArgumentException(emsg); 
      }
      String destinationName = getParameter(params, 1);
      if(StringUtils.isBlank(sourceName))
      {
         emsg = "Destination Field is required"; 
         log.error(emsg); 
         throw new IllegalArgumentException(emsg); 
      }
      String onlyIfEmpty =  getParameter(params, 2);
    
      String fieldSizeString = getParameter(params,3);
      int fieldSize = -1;
      if(StringUtils.isNotBlank(fieldSizeString) && 
    		  StringUtils.isNumeric(fieldSizeString)){
    	  fieldSize = Integer.parseInt(fieldSizeString);
      }
      
      String cleanupString = getParameter(params,4);
      boolean cleanup=false;
      if(StringUtils.isNotBlank(cleanupString)){
    	  cleanup = cleanupString.equalsIgnoreCase("true");
      }
      
      String includesExtensionString = getParameter(params,5);
      boolean includesExtension = false;
      if(StringUtils.isNotBlank(includesExtensionString)){
    	  includesExtension= includesExtensionString.equalsIgnoreCase("true");
      }

      String forceLowerString = getParameter(params,6);
      boolean forceLower = false;
      if(StringUtils.isNotBlank(forceLowerString)){
    	  forceLower = forceLowerString.equalsIgnoreCase("true");
   	  }
    		  
      String doNotCopyEmptyString = getParameter(params,7);
      boolean doNotCopyEmpty = false;
      if(StringUtils.isNotBlank(doNotCopyEmptyString)){
    	  doNotCopyEmpty = doNotCopyEmptyString.equalsIgnoreCase("true");  
      }
      
      String stripExtensionString = getParameter(params,8);
      boolean stripExtension = false;
      if(StringUtils.isNotBlank(stripExtensionString)){
    	  stripExtension = stripExtensionString.equalsIgnoreCase("true");
      }
      
      
      String prefix = getParameter(params,9);
      if(prefix == null){
    	  prefix = "";
      }
      
      String suffix = getParameter(params,10);
      if(suffix == null){
    	  suffix = "";
      }
      
      String forceExtension = getParameter(params,11);
      if(forceExtension == null){
    	  forceExtension = "";
      }
      String s = (String)request.getParameterObject(sourceName);
      String d = (String)request.getParameterObject(destinationName);
      
      if (s != null && (!doNotCopyEmpty || s.length()>0)) {
    	  if (!onlyIfEmpty.equals("true") || d==null || d.trim().length()==0) {
    		  if (fieldSize > 0 && (s.length() > fieldSize)){ s=s.substring(0,fieldSize);}
    		  if (cleanup) {
    			  s = PathCleanupUtils.cleanupPathPart(s, forceLower, includesExtension,stripExtension,prefix,suffix,forceExtension);
    		  }
    		  request.setParameter(destinationName, s);
    	  }
      }
         
   }


   // see IPSRequestPreProcessor
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // nothing to do
   }

   /**
    * Get a parameter from the parameter array, and return it as a string.
    *
    * @param params array of parameter objects from the calling function.
    * @param index the integer index into the parameters
    * 
    * @return a not-null, not-empty string which is the value of the parameter
     **/
   private static String getParameter(Object[] params, int index)
   {
      if (params.length < index + 1 || null == params[index] ||
            params[index].toString().trim().length() == 0)
      {
         return "";
      }
      else
      {
         return params[index].toString().trim();
      }
   }

}
