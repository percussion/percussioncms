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

package com.percussion.server.cache;

import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Exit to expose flush assembler capablilities to application resources.
 */
public class PSExitFlushAssemblerCache extends PSDefaultExtension 
   implements IPSRequestPreProcessor
{
   /**
    * Flushes assembler items from the cache using the provided key values. The 
    * keys may be omitted by setting <code>null</code> or an empty 
    * <code>String</code> as a value for that key.  Omitting all keys will flush 
    * all assembler pages from the cache. 
    * 
    * @param params An array of parameters expected by this exit. May not be 
    * <code>null</code>.  The parameters required for flushing the assembler 
    * cache are as follows:
    * <table border="1">
    *   <tr><th>Key</th><th>Value</th><th>Required?</th><tr>
    *   <tr>
    *     <td>appname</td>
    *     <td>The name of the Application.
    *     </td>
    *     <td>yes</td>
    *   </tr>
    *   <tr>
    *     <td>contentid</td>
    *     <td>The contentid as a <code>String</code>. If a value is specified, 
    *         expects to be a numeric value.
    *     </td>
    *     <td>yes</td>
    *   </tr>
    *   <tr>
    *     <td>revisionid</td>
    *     <td>The revisionid as a <code>String</code>. If the contentid is 
    *         omitted by specifying an empty value, then this key must also be 
    *         omitted.  If a value is specified, expects to be a numeric value.
    *     </td>
    *     <td>yes</td>
    *   </tr>
    *   <tr>
    *     <td>variantid</td>
    *     <td>The variatntId as a <code>String</code>. If a value is specified, 
    *         expects to be a numeric value.
    *     </td>
    *     <td>yes</td>
    *   </tr>
    * </table>
    * 
    * @param request The request context, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if params is <code>null</code> or empty
    * or request is <code>null</code>
    * 
    * @throws PSParameterMismatchException if <code>params</code> length is less
    * than 4, or if a parameter specifies an invalid value.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSParameterMismatchException
   {
      if(params == null || params.length == 0)
         throw new IllegalArgumentException("params may not be null or empty.");
         
      if(request == null)
         throw new IllegalArgumentException("request may not be null");       
      
      if(params.length < 4)
      {
         throw new PSParameterMismatchException(
            "A parameter is missing.");   
      }
      
      // get the assembler cache handler
      PSCacheManager mgr = PSCacheManager.getInstance();
      IPSCacheHandler handler = mgr.getCacheHandler(
         PSAssemblerCacheHandler.HANDLER_TYPE);
      
      // if caching not enabled, will be null
      if (handler == null)
         return;
         
      // create key map with all empty values
      String[] keys = handler.getKeyNames();
      int numKeys = keys.length;
      
      Map keyMap = new HashMap(numKeys);
      for (int i = 0; i < numKeys; i++) 
      {    
         keyMap.put(keys[i], "");
      }
      
      // walk keys supplied and set their values
      for (int i = 0; i < params.length; i++) 
      {    
         if (params[i] != null)
            keyMap.put(keys[i], params[i].toString());
      }

      try 
      {
         handler.validateKeys( keyMap );
      }
      catch(PSSystemValidationException e)
      {
         throw new PSParameterMismatchException( e.getLocalizedMessage() );
      }
      
      mgr.flush(keyMap);

   }
}
