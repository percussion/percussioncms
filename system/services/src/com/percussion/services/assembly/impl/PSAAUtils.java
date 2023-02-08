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
package com.percussion.services.assembly.impl;

import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.error.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

/**
 * A utility class with static methods. This will be exposed to velocity macros.
 * Only the methods that are needed 
 */
public class PSAAUtils
{   
   /**
    * The logger to use in this class
    */
   private static final Logger ms_logger = LogManager.getLogger(PSAAUtils.class);
   
   /**
    * Gets the page active assembly object id
    * 
    * @see com.percussion.content.ui.aa.PSAAObjectId for further details.
    * @param item The current working item, must not be <code>null</code>.
    * @return String representaion of JSONArray object id.
    * @throws PSAssemblyException
    * @throws PSMissingBeanConfigurationException
    * @throws JSONException
    */
   @IPSJexlMethod(description = "Creates active assembly object id for page and returns JSON string", params =
   {
      @IPSJexlParam(name = "item", type = "PSAssemblyWorkItem", description = "Current assembly item to look for the assembly parameters")
   }, returns = "JSONArray string to uniquely identify the parent page")
   public static String getPageObjectId(IPSAssemblyItem item) throws PSAssemblyException, PSMissingBeanConfigurationException, JSONException
   {
      if(item == null)
         throw new IllegalArgumentException("item must not be null");
      PSAAObjectId objid = new PSAAObjectId(item);
      return objid.toString();
   }

   /**
    * Gets the slot active assembly object id
    * 
    * @see com.percussion.content.ui.aa.PSAAObjectId for further details.
    * @param item The current working item, must not be <code>null</code>.
    * @param slotname The name of the slot must not be <code>null</code>.
    * @return objectid as a string, never <code>null</code> or empty. 
    * @throws PSAssemblyException
    * @throws PSMissingBeanConfigurationException
    * @throws IllegalArgumentException
    * @throws JSONException
    */
   @IPSJexlMethod(description = "Creates active assembly object id for slot and returns JSON string", params =
   {
      @IPSJexlParam(name = "item", type = "PSAssemblyWorkItem", description = "Current assembly item to look for the assembly parameters"),
      @IPSJexlParam(name = "slotName", type = "String", description = "Slot name")
   }, returns = "JSON object string to uniquely identify the slot on the page/snippet")
   public static String getSlotObjectId(IPSAssemblyItem item, String slotname) throws PSAssemblyException, PSMissingBeanConfigurationException, IllegalArgumentException, JSONException
   {
      if(item == null)
         throw new IllegalArgumentException("item must not be null");
      if(slotname == null)
         throw new IllegalArgumentException("slotname must not be null");
      PSAAObjectId objid = new PSAAObjectId(PSAANodeType.valueOf(1),item,slotname, null);
      return objid.toString();
   }

   /**
    * Gets the snippet active assembly object id.
    * 
    * @see com.percussion.content.ui.aa.PSAAObjectId for further details.
    * @param item The current working item, must not be <code>null</code>.
    * @param slotname The name of the slot must not be <code>null</code>.
    * @return objectid as a string, never <code>null</code> or empty. 
    * @throws PSAssemblyException
    * @throws PSMissingBeanConfigurationException
    * @throws IllegalArgumentException
    * @throws JSONException
    */
   @IPSJexlMethod(description = "Creates active assembly object id for snippet and returns JSON string", params =
   {
      @IPSJexlParam(name = "item", type = "PSAssemblyWorkItem", description = "Current assembly item to look for the assembly parameters"),
      @IPSJexlParam(name = "slotName", type = "String", description = "Slot name")
   }, returns = "JSON object string to uniquely identify the snippet in a page")
   public static String getSnippetObjectId(IPSAssemblyItem item, String slotname) throws PSAssemblyException, PSMissingBeanConfigurationException, IllegalArgumentException, JSONException
   {
      if(item == null)
         throw new IllegalArgumentException("item must not be null");
      if(slotname == null)
         throw new IllegalArgumentException("slotname must not be null");
      
      String sortrank = "0";
      
      try
      {
         sortrank = getSortRank(item);
      }
      catch(NumberFormatException e)
      {
         ms_logger.debug(e);
         ms_logger.debug("Defaulting to 0");
      }
      catch (PSException e)
      {
         ms_logger.debug(e);
         ms_logger.debug("Defaulting to 0");
      }

      PSAAObjectId objid = new PSAAObjectId(PSAANodeType.valueOf(2), item,
         slotname, sortrank);
      return objid.toString();
   }

   /**
    * Extract the sort rank for the assembly item.
    * 
    * @param item assembly item must not be <code>null</code>.
    * @return sort rank of the assembly itemn as string, may be <code>null</code>.
    * @throws PSMissingBeanConfigurationException if relationship service could
    * not be loaded.
    * @throws NumberFormatException if the relationshipid is not parsable as a
    * number.
    * @throws PSException relationship could not be loaded for any othe reason.
    */
   private static String getSortRank(IPSAssemblyItem item)
      throws PSMissingBeanConfigurationException,
      PSException
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      String sortrank = null;
      IPSRelationshipService relsvc = PSRelationshipServiceLocator
         .getRelationshipService();
      String relationshipId = item.getParameterValue(
         IPSHtmlParameters.SYS_RELATIONSHIPID, "");
      int relid = -1;
      if (!StringUtils.isBlank(relationshipId))
      {
         relid = Integer.parseInt(relationshipId);
         final PSRelationship rel = relsvc.loadRelationship(relid);
         sortrank = rel.getProperty(IPSHtmlParameters.SYS_SORTRANK);
         if (StringUtils.isBlank(sortrank))
            sortrank = "0";
         else
            sortrank = sortrank.trim(); 
      }
      return sortrank;
   }

   /**
    * Gets the page active assembly object id
    * 
    * @see com.percussion.content.ui.aa.PSAAObjectId for further details.
    * @param item The current working item, must not be <code>null</code>.
    * @return String representaion of JSONArray object id.
    * @throws PSAssemblyException
    * @throws PSMissingBeanConfigurationException
    * @throws JSONException
    */
   @IPSJexlMethod(description = "Creates active assembly object id for field and returns JSON string", params =
   {
      @IPSJexlParam(name = "item", type = "PSAssemblyWorkItem", description = "Current assembly item to look for the assembly parameters"),
      @IPSJexlParam(name = "fieldName", type = "String", description = "Field name")
   }, returns = "JSONArray string to uniquely identify the parent page")
   public static String getFieldObjectId(IPSAssemblyItem item, String fieldName) throws PSAssemblyException, PSMissingBeanConfigurationException, IllegalArgumentException, JSONException
   {
      if (item == null)
         throw new IllegalArgumentException("item must not be null");
      PSAAObjectId objid = new PSAAObjectId(PSAANodeType.valueOf(3), item,
            fieldName, null);
      return objid.toString();
   }
   
}
