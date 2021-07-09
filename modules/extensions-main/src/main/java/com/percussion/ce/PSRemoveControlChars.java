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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
      StringBuffer sb = new StringBuffer(s);      
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
