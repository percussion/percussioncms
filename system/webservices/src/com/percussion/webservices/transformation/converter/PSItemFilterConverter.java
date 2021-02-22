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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.IPSItemFilterRuleDef;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.system.PSFilterRule;
import com.percussion.webservices.system.PSFilterRuleParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;

/**
 * Converts objects between the classes
 * <code>com.percussion.services.filter.data.PSItemFilter</code> and
 * <code>com.percussion.webservices.system.PSItemFilter</code>.
 */
public class PSItemFilterConverter extends PSConverter
{
   /*
    * (non-Javadoc)
    * 
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSItemFilterConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);

      m_specialProperties.add("filter_id");
      m_specialProperties.add("id");
      m_specialProperties.add("parentFilter");
      m_specialProperties.add("parentFilterId");
      m_specialProperties.add("legacy_authtype");
      m_specialProperties.add("legacyAuthtype");
      m_specialProperties.add("rules");
   }

   /*
    * (non-Javadoc)
    * 
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      Object result = super.convert(type, value);

      try
      {
         if (isClientToServer(value))
         {
            com.percussion.webservices.system.PSItemFilter orig = (com.percussion.webservices.system.PSItemFilter) value;

            PSItemFilter dest = (PSItemFilter) result;

            // convert id
            dest.setGUID(new PSDesignGuid(orig.getId()));

            // convert parent filter
            long parentId = orig.getParentFilterId();
            if (parentId != 0)
               dest.setParentFilter(loadFilter(parentId));

            // convert authtype
            int authtype = orig.getLegacyAuthtype();
            if (authtype != -1)
               dest.setLegacyAuthtypeId(orig.getLegacyAuthtype());

            // convert rules
            convertRules(orig, dest);
         }
         else
         {
            PSItemFilter orig = (PSItemFilter) value;

            com.percussion.webservices.system.PSItemFilter dest = (com.percussion.webservices.system.PSItemFilter) result;

            // convert id
            dest.setId(new PSDesignGuid(orig.getGUID()).getValue());

            // convert parent filter
            IPSGuid parentId = orig.getParentFilterId();
            if (parentId != null)
               dest.setParentFilterId(new PSDesignGuid(parentId).getValue());

            // convert authtype
            Integer authtype = orig.getLegacyAuthtypeId();
            if (authtype != null)
               dest.setLegacyAuthtype(orig.getLegacyAuthtypeId());
            else
               dest.setLegacyAuthtype(-1);

            // convert rules
            Set<IPSItemFilterRuleDef> rules = orig.getRuleDefs();
            if (rules == null || rules.isEmpty())
               dest.setRules(new PSFilterRule[0]);
            else
            {
               PSFilterRule[] destRules = new PSFilterRule[rules.size()];
               int index = 0;
               for (IPSItemFilterRuleDef rule : rules)
               {
                  if (rule == null) // only possible with WB side converter
                     continue;
                  PSFilterRule destRule = new PSFilterRule();
                  destRule.setName(rule.getRuleName());
                  destRule.setParameters(convertParams(rule.getParams()));

                  destRules[index++] = destRule;
               }
               dest.setRules(destRules);
            }
         }
      }
      catch (PSFilterException | PSNotFoundException e)
      {
         throw new ConversionException(e);
      }

      return result;
   }

   /**
    * The WB converter can override this to merge rules without hitting server.
    * 
    * @param orig web services item filter, must not be <code>null</code>.
    * @param dest the service item filter object, must not be <code>null</code>
    * and is assumed to be taken care of other field transformation.
    * @throws PSFilterException
    */
   protected void convertRules(
      com.percussion.webservices.system.PSItemFilter orig,
      PSItemFilter dest) throws PSFilterException, PSNotFoundException {
      if (orig == null)
      {
         throw new IllegalArgumentException("orig must not be null");
      }
      if (dest == null)
      {
         throw new IllegalArgumentException("dest must not be null");
      }
      IPSFilterService service = PSFilterServiceLocator.getFilterService();
      IPSItemFilter currentFilter = null;
      /* We need to load the filter if it exists and not use the one
       * returned by the find method as we need to modify the filter.
       */
      if(service.findFilterByID(new PSDesignGuid(orig.getId())) != null)
         currentFilter = loadFilter(orig.getId());
      Set<IPSItemFilterRuleDef> currentRules = null;
      if (currentFilter == null)
         currentRules = new HashSet<>();
      else
         currentRules = currentFilter.getRuleDefs();
      PSFilterRule[] rules = orig.getRules();
      Set<IPSItemFilterRuleDef> destRules = new HashSet<>();
      for (PSFilterRule rule : rules)
      {
         if (rule == null) // only possible with WB side converter
            continue;
         IPSItemFilterRuleDef destRule = null;
         Iterator walker = currentRules.iterator();
         while (walker.hasNext() && destRule == null)
         {
            IPSItemFilterRuleDef test = (IPSItemFilterRuleDef) walker.next();
            String currentName = test.getRuleName();
            if (currentName != null && currentName.equals(rule.getName()))
               destRule = test;
         }

         PSFilterRuleParam[] params = rule.getParameters();
         if (destRule != null)
         {
            currentRules.remove(destRule);

            // update existing rule
            for (String paramName : destRule.getParams().keySet())
               destRule.removeParam(paramName);
            for (PSFilterRuleParam param : params)
               destRule.setParam(param.getName(), param.getValue());
         }
         else
         {
            // create a new rule
            Map<String, String> destParams = new HashMap<>();
            for (PSFilterRuleParam param : params)
               destParams.put(param.getName(), param.getValue());

            destRule = createRuleDef(rule.getName(), destParams);
         }

         destRules.add(destRule);
      }
      dest.setRuleDefs(destRules);
   }

   /**
    * Loads the filter for the supplied id through the
    * <code>IPSFilterService</code>.
    * 
    * @param id th full id including type and uuid.
    * @return the filter for the supplied id, never <code>null</code>.
    * @throws PSFilterException if no filter is found for the supplied id.
    */
   protected IPSItemFilter loadFilter(long id) throws PSFilterException, PSNotFoundException {
      IPSFilterService service = PSFilterServiceLocator.getFilterService();

      List<IPSGuid> ids = new ArrayList<>();
      ids.add(new PSDesignGuid(id));
      List<IPSItemFilter> filters = service.loadFilter(ids);

      return filters.get(0);
   }

   /**
    * Convert the supplied server side parameters to an array of client side
    * parameters.
    * 
    * @param params the server side parameters, may be <code>null</code> or
    * empty.
    * @return the client sie parameter array, never <code>null</code>, may be
    * empty.
    */
   private PSFilterRuleParam[] convertParams(Map<String, String> params)
   {
      if (params == null || params.size() == 0)
         return new PSFilterRuleParam[0];

      PSFilterRuleParam[] destParams = new PSFilterRuleParam[params.size()];
      int index = 0;
      for (String name : params.keySet())
      {
         PSFilterRuleParam destParam = new PSFilterRuleParam();
         destParam.setName(name);
         destParam.setValue(params.get(name));

         destParams[index++] = destParam;
      }

      return destParams;
   }

   /**
    * Create a new rule definition for the summplied name and parameters.
    * 
    * @param name the name of the new rule, assumed not <code>null</code> or
    * empty.
    * @param params the rule parameters, assumed not <code>null</code>, may
    * be empty.
    * @return the new created item filter rule, never <code>null</code>.
    * @throws PSFilterException if no rule was found for the supplied name.
    */
   protected IPSItemFilterRuleDef createRuleDef(String name,
      Map<String, String> params) throws PSFilterException
   {
      IPSFilterService service = PSFilterServiceLocator.getFilterService();

      return service.createRuleDef(name, params);
   }
}
