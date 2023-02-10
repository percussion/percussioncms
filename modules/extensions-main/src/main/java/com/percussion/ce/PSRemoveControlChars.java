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
package com.percussion.ce;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This exit will remove all control characters from all fields in
 * a content editor. These characters are illegal in XML and will
 * cause an exception if they are left in. This exit is added to the
 * modify command handler in the ContentEditorSystemDef as an input data
 * exit.
 */
public class PSRemoveControlChars implements IPSRequestPreProcessor 
{

   /* 
    * @see com.percussion.extension.IPSRequestPreProcessor#preProcessRequest(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
            throws PSAuthorizationException, PSRequestValidationException,
            PSParameterMismatchException, PSExtensionProcessingException
   {
           
      Map reqParams = request.getParameters();
      Set keys = reqParams.keySet();
      Iterator it = keys.iterator();
      String key = null;
      Object value = null;
      while(it.hasNext())
      {
         key = (String)it.next();
         value = reqParams.get(key);
         if(value instanceof String)
         {
            value = removeControlChars((String)value);
            // Only update parameter if a control character was found
            // and removed.
            if(value != null)
               reqParams.put(key, value);
         }
         
      }
      
   }
   
   /**
    * Removes control characters from the specified string
    * 
    * @param s the string to be filtered, may be <code>null</code>
    * or empty.
    * @return the string with all control characters removed. May be
    * <code>null</code> or empty.
    */
   private String removeControlChars(String s)
   {
      boolean isModified = false;
      if(s == null)
         return null;
      StringBuilder sb = new StringBuilder(s);
      for(int i = 0; i < sb.length(); i++)
      {
         char ch = sb.charAt(i);
         if(Character.isISOControl(ch))
         {   
            isModified = true;
            sb.deleteCharAt(i--);
         }
      }         
      return isModified ? sb.toString(): null;
   }
   
   /* 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
            throws PSExtensionException
   {
      // Does nothing
   }   
  
}
