/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.content.ui.aa;

import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
   private static Log ms_logger = LogFactory.getLog(PSAAUtils.class);
   
   /**
    * Gets the page active assembly object id
    * 
    * @see PSAAObjectId for further details.
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
    * @see PSAAObjectId for further details.
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
    * @see PSAAObjectId for further details.
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
    * @see PSAAObjectId for further details.
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
