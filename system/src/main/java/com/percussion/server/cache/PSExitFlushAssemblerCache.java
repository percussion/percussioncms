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
