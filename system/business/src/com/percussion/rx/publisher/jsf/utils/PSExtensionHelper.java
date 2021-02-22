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
package com.percussion.rx.publisher.jsf.utils;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSExtensionParamDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.publisher.jsf.data.PSParameter;
import com.percussion.server.PSServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;

/**
 * Helper static methods for use with extensions.
 * 
 * @author dougrand
 */
public class PSExtensionHelper
{
   /**
    * @param extensionName the name of the extension, never <code>null</code> 
    * or empty.
    * @return the skeleton list of parameters, ready to be populated, never 
    * <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public static List<PSParameter> getParametersForExtension(String extensionName)
   {
      if (StringUtils.isBlank(extensionName))
      {
         throw new IllegalArgumentException(
               "extensionName may not be null or empty");
      }
      IPSExtensionManager emgr = PSServer.getExtensionManager(null);
      List<PSParameter> rval = new ArrayList<>();
      PSExtensionRef ref = new PSExtensionRef(extensionName);
      try
      {
         IPSExtensionDef def = emgr.getExtensionDef(ref);
         Iterator<String> niter = def.getRuntimeParameterNames();
         while (niter.hasNext())
         {
            String name = niter.next();
            IPSExtensionParamDef param = def.getRuntimeParameter(name);
            rval.add(new PSParameter(name, param.getDescription(), null));
         }
      }
      catch (Exception e)
      {
         // Don't bother throwing an exception for this case
      }
      return rval;
   }
   
   /**
    * In the interface we often need a list from the map we've retrieved or
    * manipulated. This populates the list.
    * 
    * @param input list to be populated from the map, never <code>null</code>.
    * @param params the parameter map, may be <code>null</code>.
    */
   public static void populateListFromMap(List<PSParameter> input, 
         Map<String, String> params)
   {
      if (input == null)
      {
         throw new IllegalArgumentException("input may not be null");
      }
      if (params != null)
      {
         for(PSParameter p : input)
         {
            String value = params.get(p.getName());
            p.setValue(value);
         }
      }
   }

   /**
    * Traverse the passed in parameter list and save the values in the supplied
    * map.
    * 
    * @param params the parameters, never <code>null</code>.
    * @param savedData the map to save the data in, never <code>null</code>.
    */
   public static void saveParameterData(List<PSParameter> params,
         Map<String, String> savedData)
   {
      if (params == null)
      {
         throw new IllegalArgumentException("params may not be null");
      }
      if (savedData == null)
      {
         throw new IllegalArgumentException("savedData may not be null");
      }
      for(PSParameter p : params)
      {
         if (p.getValue() != null)
            savedData.put(p.getName(), p.getValue());
      }
   }
   
   /**
    * Get the list of registered extensions that implement {@link IPSEditionTask}.
    * 
    * @return selection items, in each item, the value is the fully 
    *    qualified name of the extension and the label is the name of the 
    *    extension. It never <code>null</code>, but may be empty.
    */
   @SuppressWarnings("unchecked")
   public static List<SelectItem> getTaskExtensionChoices(String interfaceName)
   {
      List<SelectItem> rval = new ArrayList<>();
      try
      {
         IPSExtensionManager emgr = PSServer.getExtensionManager(null);


         Iterator iter = emgr.getExtensionNames(null, null, interfaceName,
               null);
         while (iter.hasNext())
         {
            PSExtensionRef ref = (PSExtensionRef) iter.next();
            String name = ref.getFQN();
            String display = ref.getExtensionName();
            rval.add(new SelectItem(name, display));
         }
      }
      catch (Exception e)
      {
         // Return none on error
      }
      return rval;
   }

   /**
    * Lookup the extension name and set the set of exposed names, used to
    * filter the parameters. Then populate and/or extend the list of
    * targeted parameters.
    * 
    * @param extName the name of the extension, it may be <code>null</code> or 
    *    empty. Do nothing if it is <code>null</code> or empty.
    * @param srcParams the source parameters to be combined into the target 
    *    parameter if there is any, never <code>null</code>, but may be empty.
    * @param tgtParams the target parameters, never <code>null</code>, but 
    *    may be empty.
    *    
    * @return the possible modified target parameters. It is sorted by the
    *    name of the parameter if it is modified, never <code>null</code>, may
    *    be empty.
    */
   public static List<PSParameter> setupParameters(String extName,
         Map<String, String> srcParams, List<PSParameter> tgtParams)
   {
      if (StringUtils.isBlank(extName))
         return tgtParams;

      Map<String, String> savedData = new HashMap<>();
      savedData.putAll(srcParams);

      PSExtensionHelper.saveParameterData(tgtParams, savedData);
      tgtParams = PSExtensionHelper.getParametersForExtension(extName);
      PSExtensionHelper.populateListFromMap(tgtParams, savedData);

      Collections.sort(tgtParams);
      
      return tgtParams;
   }

   
}
