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

package com.percussion.extension;

import com.percussion.design.objectstore.IPSReplacementValue;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple abstract class that has a default implementation for the
 * IPSExtension interface methods. By default they do nothing.
 *
 * @author     Jian Huang
 * @version    2.0
 * @since      1.1
 */
public abstract class PSDefaultExtension implements IPSExtension
{
   /**
    * This is a default implementation that does nothing except save the input
    * values in protected local storage for use by derived classes.
    * <p>
    * See {@link IPSExtension#init(IPSExtensionDef, File) init} for details.
    */
   public void init( IPSExtensionDef def, File codeRoot )
      throws PSExtensionException
   {
      m_def = def;
      m_codeRoot = codeRoot;
   }
   
   /**
    * Get the parameter map.
    * 
    * @param params an array with all supplied extension parameter values,
    *    may be <code>null</code> or empty.
    * @return a map with the parameter name as key and the parameter value
    *    as <code>String</code> object, never <code>null</code>, may be empty. 
    *    Parameter values may be <code>null</code>.
    */
   protected Map<String, String> getParameters(Object[] params)
   {
      Map<String, String> parameters = new HashMap<>();
      
      if (params != null)
      {
         int index = 0;
         Iterator names = m_def.getRuntimeParameterNames();
         while (names.hasNext())
         {
            String name = (String) names.next();
         
            if (params.length > index)
               parameters.put(name, 
                  ((IPSReplacementValue) params[index]).getValueText());
            else
               parameters.put(name, null);
            
            index++;
         }
      }
      
      return parameters;
   }

   /**
    * This is the definition for this extension. You may want to use it for
    * validation purposes in the <code>process</code> method.
    */
   protected IPSExtensionDef m_def;

   /**
    * This value contains the 'root' directory for this extension. When
    * installed, all files are installed relative to this location. Files can
    * be loaded from anywhere under this directory and no where else (by
    * default, the actual security policy may vary). This object could be used
    * to load a property file when executing the UDF.
    */
   protected File m_codeRoot;
}
