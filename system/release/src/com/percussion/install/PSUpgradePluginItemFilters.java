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
package com.percussion.install;

import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.IPSItemFilterRuleDef;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * Upgrade item filters as part of installation. This plugin:
 * <ul>
 * <li>Add the preview filter rule to the preview item filter
 * <li>Add the public filter rule to the public item filter
 * </ul>
 * 
 * @author dougrand
 * 
 */
public class PSUpgradePluginItemFilters implements IPSUpgradePlugin
{
   /**
    * Eponymously named constant
    */
   private static final String PREVIEW_FILTER_RULE = "Java/global/percussion/itemfilter/sys_previewFilter";
   /**
    * Eponymously named constant
    */
   private static final String PREVIEW_FILTER = "preview";
   /**
    * Eponymously named constant
    */
   private static final String PUBLIC_FILTER_RULE = "Java/global/percussion/itemfilter/sys_publicFilter";
   /**
    * Eponymously named constant
    */
   private static final String PUBLIC_FILTER = "public";

   /**
    * Filter service
    */
   private IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();

   /**
    * Perform upgrade
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      int respType = PSPluginResponse.SUCCESS;
      String respMsg = "";
      
      try
      {
         checkAndAddRule(PREVIEW_FILTER, PREVIEW_FILTER_RULE,
               Collections.EMPTY_MAP);
         checkAndAddRule(PUBLIC_FILTER, PUBLIC_FILTER_RULE,
               Collections.EMPTY_MAP);
      }
      catch (Exception e)
      {
         respType = PSPluginResponse.EXCEPTION;
         respMsg = e.getMessage();
         e.printStackTrace(config.getLogStream());
      }

      return new PSPluginResponse(respType, respMsg);
   }

   /**
    * Check the given filter (if found) for the presence of the given rule. If
    * not there, then add the rule with the supplied parameters.
    * 
    * @param filtername the name of the filter, assumed never <code>null</code>
    *           or empty.
    * @param rulename the name of the rule, assumed never <code>null</code> or
    *           empty.
    * @param params the parameters, may be <code>null</code>.
    */
   private void checkAndAddRule(String filtername, String rulename,
         Map params)
   {
      try
      {
         IPSItemFilter filter = fsvc.findFilterByName(filtername);
         boolean found = false;
         Iterator fiter = filter.getRuleDefs().iterator();
         while (fiter.hasNext())
         {
            IPSItemFilterRuleDef def = (IPSItemFilterRuleDef) fiter.next();
            if (def.getRuleName().equals(rulename))
            {
               found = true;
               break;
            }
         }
         if (!found)
         {
            filter.addRuleDef(fsvc.createRuleDef(rulename, params));
            fsvc.saveFilter(filter);
         }
      }
      catch (PSFilterException e)
      {
         // If not found then ignore
      }
   }

   /**
    * Test method
    * @param args ignored
    */
   public static void main(String[] args)
   {
      PSUpgradePluginItemFilters plugin =
         new PSUpgradePluginItemFilters();
      plugin.process(null, null);
   }
}
