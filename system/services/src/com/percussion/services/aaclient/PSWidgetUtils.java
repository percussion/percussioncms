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
package com.percussion.services.aaclient;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;

/**
 * Utility class with static methods intended to be used in the AA interface.
 */
public class PSWidgetUtils
{
   /**
    * @param item
    * @return
    */
   @IPSJexlMethod(description = "helper to convert map of multi valued map from the supplied assembly item to a map of single values", params =
   {
      @IPSJexlParam(name = "item", type = "PSAssemblyWorkItem", description = "Current assembly item to look for the assembly parameters")
   }, returns = "Map of name and single value counter parts of the assembly parameters")
   static public Map<String, String> getParams(IPSAssemblyItem item)
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      Map<String, String> params = new HashMap<>();
      Map oldParams = item.getParameters();
      Iterator iter = oldParams.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String) iter.next();
         String[] val = (String[]) oldParams.get(key);
         if (val != null && val.length > 0)
            params.put(key, val[0]);
      }

      params.put(IPSHtmlParameters.SYS_VARIANTID, String.valueOf(item
         .getTemplate().getGUID().getUUID()));

      return params;
   }

   /**
    * @param item
    * @return
    * @throws PSMissingBeanConfigurationException
    * @throws PSAssemblyException
    */
   @IPSJexlMethod(description = "helper to parse assembly parameters for the supplied assembly page and return a JSON string", params =
   {
      @IPSJexlParam(name = "item", type = "PSAssemblyWorkItem", description = "Current assembly item to look for the assembly parameters")
   }, returns = "JSON object string to uniquely identify the parent page")
   static public String parseParentObjectId(IPSAssemblyItem item)
      throws PSAssemblyException, PSMissingBeanConfigurationException
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      Map params = getParams(item);
      JSONObject obj = new JSONObject();
      obj.putAll(parseObjectId(params, PSWidgetNodeType.WIDGET_NODE_TYPE_PAGE));
      return obj.toString();
   }

   /**
    * @param item
    * @return
    * @throws PSMissingBeanConfigurationException
    * @throws PSAssemblyException
    */
   @IPSJexlMethod(description = "helper to parse assembly parameters for the supplied assembly page/snippet and slot and return a JSON string", params =
   {
      @IPSJexlParam(name = "item", type = "PSAssemblyWorkItem", description = "Current assembly item to look for the assembly parameters"),
      @IPSJexlParam(name = "slotName", type = "String", description = "Slot name")
   }, returns = "JSON object string to uniquely identify the slot on the page/snippet")
   static public String parseSlotObjectId(IPSAssemblyItem item,
      String slotName) throws PSAssemblyException,
      PSMissingBeanConfigurationException
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      if (StringUtils.isEmpty(slotName))
      {
         throw new IllegalArgumentException(
            "slotName must not be null or empty");
      }
      int slotid = PSAssemblyServiceLocator.getAssemblyService()
         .findSlotByName(slotName).getGUID().getUUID();
      Map params = getParams(item);
      params.put(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slotid));
      JSONObject obj = new JSONObject();
      obj.putAll(parseObjectId(params, PSWidgetNodeType.WIDGET_NODE_TYPE_SLOT));
      return obj.toString();
   }

   /**
    * @param item
    * @return
    * @throws PSMissingBeanConfigurationException
    * @throws PSAssemblyException
    */
   @IPSJexlMethod(description = "helper to parse assembly parameters for the supplied assembly snippet and return a JSON string", params =
   {
      @IPSJexlParam(name = "item", type = "PSAssemblyWorkItem", description = "Current assembly item to look for the assembly parameters")
   }, returns = "JSON object string to uniquely identify the snippet in a page")
   static public String parseSnippetObjectId(IPSAssemblyItem item,
      String slotName) throws PSAssemblyException,
      PSMissingBeanConfigurationException
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      if (StringUtils.isEmpty(slotName))
      {
         throw new IllegalArgumentException(
            "slotName must not be null or empty");
      }
      int slotid = PSAssemblyServiceLocator.getAssemblyService()
         .findSlotByName(slotName).getGUID().getUUID();
      Map params = getParams(item);
      params.put(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slotid));
      JSONObject obj = new JSONObject();
      obj.putAll(parseObjectId(params,
         PSWidgetNodeType.WIDGET_NODE_TYPE_SNIPPET));
      return obj.toString();
   }

   /**
    * 
    * @param params
    * @param nodeType
    * @return
    * @throws PSMissingBeanConfigurationException
    * @throws PSAssemblyException
    */
   static public Map parseObjectId(Map params, PSWidgetNodeType nodeType)
      throws PSAssemblyException, PSMissingBeanConfigurationException
   {
      if (params == null)
      {
         throw new IllegalArgumentException("params must not be null");
      }
      Map<String, String> id = parseCommonParams(params);
      // Added to all types of nodes is the node type
      id.put(ATTR_NODETYPE, String.valueOf(nodeType.getOrdinal()));
      String temp;
      switch (nodeType)
      {
         case WIDGET_NODE_TYPE_PAGE:
            // Revision required
            temp = parseParam(params, IPSHtmlParameters.SYS_REVISION, null,
               true);
            id.put(IPSHtmlParameters.SYS_REVISION, temp.toString());
            break;
         case WIDGET_NODE_TYPE_SLOT:
            // slotid required
            temp = parseParam(params, IPSHtmlParameters.SYS_SLOTID, null, true);
            id.put(IPSHtmlParameters.SYS_SLOTID, temp.toString());
            break;
         case WIDGET_NODE_TYPE_SNIPPET:
            // relationshipid required
            temp = parseParam(params, IPSHtmlParameters.SYS_RELATIONSHIPID,
               null, true);
            id.put(IPSHtmlParameters.SYS_RELATIONSHIPID, temp.toString());
            // slotid required
            temp = parseParam(params, IPSHtmlParameters.SYS_SLOTID, null, true);
            id.put(IPSHtmlParameters.SYS_SLOTID, temp.toString());
            break;
         case WIDGET_NODE_TYPE_FIELD:
            // CE field name required
            temp = parseParam(params, IPSHtmlParameters.SYS_FIELD_NAME, null,
               true);
            id.put(IPSHtmlParameters.SYS_FIELD_NAME, temp.toString());
            break;
         default:
            break;
      }
      return id;
   }

   /**
    * Helper to parse all parameters common to all node types.
    * 
    * @param params parameter map, assumed not <code>null</code>.
    * @throws PSMissingBeanConfigurationException
    * @throws PSAssemblyException
    */
   static private Map<String, String> parseCommonParams(Map params)
      throws PSAssemblyException, PSMissingBeanConfigurationException
   {
      Map<String, String> id = new HashMap<>();
      // ContentId
      String temp = parseParam(params, IPSHtmlParameters.SYS_CONTENTID, null,
         true);
      id.put(IPSHtmlParameters.SYS_CONTENTID, temp);

      // revision
      temp = parseParam(params, IPSHtmlParameters.SYS_REVISION, null, true);
      id.put(IPSHtmlParameters.SYS_REVISION, temp);

      // template name / VariantId
      temp = parseParam(params, IPSHtmlParameters.SYS_TEMPLATE, null, false);
      if (!StringUtils.isEmpty(temp))
      {
         int tid = PSAssemblyServiceLocator.getAssemblyService()
            .findTemplateByName(temp).getGUID().getUUID();
         temp = String.valueOf(tid);
      }
      else
      {
         temp = parseParam(params, IPSHtmlParameters.SYS_VARIANTID, null, true);
      }
      id.put(IPSHtmlParameters.SYS_VARIANTID, temp.toString());

      // Optional params
      // FolderId
      temp = parseParam(params, IPSHtmlParameters.SYS_FOLDERID, null, false);
      if (!StringUtils.isEmpty(temp))
         id.put(IPSHtmlParameters.SYS_FOLDERID, temp.toString());

      // Params with default values
      // SiteId
      temp = parseParam(params, IPSHtmlParameters.SYS_SITEID, "0", false);
      id.put(IPSHtmlParameters.SYS_SITEID, temp);

      // Context
      temp = parseParam(params, IPSHtmlParameters.SYS_CONTEXT, "0", false);
      id.put(IPSHtmlParameters.SYS_CONTEXT, temp.toString());

      // Authtype
      temp = parseParam(params, IPSHtmlParameters.SYS_AUTHTYPE, "0", false);
      id.put(IPSHtmlParameters.SYS_AUTHTYPE, temp);

      return id;
   }

   /**
    * Helper method to parse parameter with a given name in the supplied
    * parameter map.
    * 
    * @param params parameter map assumed not <code>null</code> or empty.
    * @param name nameof the parameter to parse, assumed not <code>null</code>
    * or empty.
    * @param defValue defualt value to be used if the parameter does not exist
    * in the supplied map, may be <code>null</code> or empty.
    * @param isRequired <code>true</code> if the parameter must have a non
    * <code>null</code> or non empty value directly or via the default value.
    * @return parsed parameter, may be <code>null</code> or empty.
    * @throws IllegalArgumentException if the value is required and the value is
    * resolved to <code>null</code> or empty via parameter map or defualt
    * value.
    */
   static private String parseParam(Map params, String name, String defValue,
      boolean isRequired)
   {
      String val = defValue;
      Object temp = params.get(name);
      if (temp != null && !StringUtils.isEmpty(temp.toString()))
         val = temp.toString();
      if (val == null && isRequired)
         throw new IllegalArgumentException(name + " must not be null or empty");
      return val;
   }

   /**
    * @param cid
    * @return
    * @throws PSMissingBeanConfigurationException
    * @throws NumberFormatException
    */
   static public PSComponentSummary getItemSummary(int cid)
      throws PSMissingBeanConfigurationException, NumberFormatException
   {
      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = objMgr.loadComponentSummary(cid);
      return summary;
   }

   // Generic node type attribute name
   public static final String ATTR_NODETYPE = "nodeType";

   public static final String ATTR_ACTION = "action";

   // Tree widget node attributes
   public static final String TREENODE_ATTR_TITLE = "title";

   public static final String ATTR_OBJECTID = "objectId";

   public static final String TREENODE_ATTR_ISFOLDER = "isFolder";

   public static final String TREENODE_ATTR_ICONSRC = "childIconSrc";
}
