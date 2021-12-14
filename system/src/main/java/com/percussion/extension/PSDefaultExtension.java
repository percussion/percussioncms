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
