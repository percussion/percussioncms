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
package com.percussion.services.publisher.ui;

import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSExtensionParamDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSServer;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.publisher.data.PSEditionType;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;

/**
 * Provides a set of getters to populate browser UIs. Each method is named for
 * the kind of data that is returned.
 * 
 * @author dougrand
 */
public class PSPublisherUI
{
   /**
    * Get the edition types
    * 
    * @return an array of edition types for use in the selection dropdown, never
    *         <code>null</code>
    */
   public SelectItem[] getEditionTypes()
   {
      int len = PSEditionType.values().length;
      SelectItem rval[] = new SelectItem[len];

      for (int i = 0; i < len; i++)
      {
         PSEditionType val = PSEditionType.values()[i];
         rval[i] = new SelectItem(val.name(), StringUtils.capitalize(val.name()
               .toLowerCase()));
      }

      return rval;
   }

   /**
    * Get the content list generators
    * 
    * @return an array of generators for use in the selection dropdown, never
    *         <code>null</code>
    */
   public SelectItem[] getGenerators()
   {
      return getItemsForExtension("com.percussion.services.publisher.IPSContentListGenerator");
   }

   /**
    * Get the template expanders
    * 
    * @return an array of template expanders for use in the selection.
    */
   public SelectItem[] getTemplateExpanders()
   {
      return getItemsForExtension("com.percussion.services.publisher.IPSTemplateExpander");
   }

   /**
    * Get the parameter names and descriptions for the given extension
    * 
    * @param extensionName the extension name, if empty or <code>null</code>
    *           an empty array is returned
    * @return an array of pairs. The first member of each pair is the param
    *         name, the second is the description.
    */
   @SuppressWarnings("unchecked")
   public PSPair<String, String>[] getParameters(String extensionName)
   {
      if (StringUtils.isBlank(extensionName))
      {
         return new PSPair[0];
      }
      return getParametersForExtension(extensionName);
   }

   /**
    * Load filters for the selection list
    * 
    * @return a list of known filters for the selection list
    */
   public SelectItem[] getFilters()
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      List<IPSItemFilter> filters = fsvc.findAllFilters();
      SortedSet<String> names = new TreeSet<>();
      for (IPSItemFilter filter : filters)
      {
         names.add(filter.getName());
      }
      int count = names.size();
      SelectItem rval[] = new SelectItem[count];
      int i = 0;
      for (String name : names)
      {
         rval[i++] = new SelectItem(name);
      }
      return rval;
   }

   /**
    * Get the selection items for a given extension interface
    * 
    * @param iface the interface, assumed not <code>null</code> or empty
    * @return the array of items
    */
   private SelectItem[] getItemsForExtension(String iface)
   {
      List<String> matching = getMatchingExtensions(iface);
      int count = matching.size();
      SelectItem rval[] = new SelectItem[count];
      for (int i = 0; i < count; i++)
      {
         String ename = matching.get(i);
         rval[i] = new SelectItem(ename, ename);
      }
      return rval;
   }

   /**
    * Get the matching extensions and return their names
    * 
    * @param iface the iface, assumed not <code>null</code> or empty
    * @return the matching names
    */
   @SuppressWarnings("unchecked")
   private List<String> getMatchingExtensions(String iface)
   {
      IPSExtensionManager mgr = PSServer.getExtensionManager(null);
      try
      {
         Iterator<PSExtensionRef> refiter = mgr.getExtensionNames(null, null,
               iface, null);
         List<String> rval = new ArrayList<>();
         while (refiter.hasNext())
         {
            PSExtensionRef ref = refiter.next();
            rval.add(ref.getFQN());
         }
         return rval;
      }
      catch (PSExtensionException e)
      {
         throw new RuntimeException("Problem getting extensions for iface "
               + iface, e);
      }
   }

   /**
    * Get the parameters for the given extension
    * 
    * @param extensionName the name of the extension
    * @return an array of pairs. Each pair consists of a parameter name and a
    *         parameter description
    */
   @SuppressWarnings("unchecked")
   private PSPair<String, String>[] getParametersForExtension(
         String extensionName)
   {
      IPSExtensionManager mgr = PSServer.getExtensionManager(null);
      try
      {
         ArrayList<PSPair<String, String>> params = new ArrayList<>();
         PSExtensionRef ref = new PSExtensionRef(extensionName);
         IPSExtensionDef def = mgr.getExtensionDef(ref);
         Iterator<String> pnames = def.getRuntimeParameterNames();
         while (pnames.hasNext())
         {
            String pname = pnames.next();
            IPSExtensionParamDef pdef = def.getRuntimeParameter(pname);
            params
                  .add(new PSPair<>(pname, pdef.getDescription()));
         }

         PSPair<String, String> rval[] = new PSPair[params.size()];
         params.toArray(rval);
         return rval;
      }
      catch (PSExtensionException e)
      {
         throw new RuntimeException("Problem loading extension "
               + extensionName, e);
      }
      catch (PSNotFoundException e)
      {
         throw new RuntimeException("Problem loading extension "
               + extensionName, e);
      }
   }
}
