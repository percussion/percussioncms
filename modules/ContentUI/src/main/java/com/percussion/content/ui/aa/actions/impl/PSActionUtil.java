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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.browse.PSContentBrowser;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.HTMLElements;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.Tag;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Various utility methods that are used by one or more
 * client actions.
 */
public abstract class PSActionUtil
{
   
   static Logger log = Logger.getLogger(PSActionUtil.class);
   
   /**
    * Private ctor to not allow instantiation
    */
   private PSActionUtil()
   {
      
   }

   /**
    * Helper method to retrieve the the assembly parameters from an 
    * object id and place them in a map for use with the assembly service.
    * Use component summary as backup for the values if not present in
    * the object id.
    * @param objectId cannot be <code>null</code>.
    * @param currentUser the current user, used to get the appropriate
    * revision. May be <code> null</code> or empty. If so
    * then just uses the summary revision.
    * @return map objectId, never <code>null</code>.
    */
   protected static Map<String, String[]> getAssemblyParams(
            PSAAObjectId objectId, String currentUser)
   {
      if(objectId == null)
         throw new IllegalArgumentException("objectId cannot be null.");
      Map<String, String[]> params = new HashMap<String, String[]>();
      Object temp = objectId.getContentId();
      params.put(IPSHtmlParameters.SYS_CONTENTID, new String[]
      {
         temp.toString()
      });

      params.put(IPSHtmlParameters.SYS_USER, new String[]
      {
         currentUser
      });
      
      temp = objectId.getVariantId();
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_VARIANTID, new String[]
         {
            temp.toString()
         });
      
      temp = objectId.getSlotId();
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_SLOTID, new String[]
         {
            temp.toString()
         });

      temp = objectId.getSiteId();
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_SITEID, new String[]
         {
            temp.toString()
         });

      temp = objectId.getFolderId();
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_FOLDERID, new String[]
         {
            temp.toString()
         });

      temp = objectId.getAuthType();
      if (temp == null)
         temp = "0";
      params.put(IPSHtmlParameters.SYS_AUTHTYPE, new String[]
      {
         temp.toString()
      });

      temp = objectId.getContext();
      if (temp == null)
         temp = "0";
      params.put(IPSHtmlParameters.SYS_CONTEXT, new String[]
      {
         temp.toString()
      });
      
      temp = objectId.getParentId();
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_CLONEDPARENTID, new String[]
         {
            temp.toString()
         });
      return params;
   }
   
   /**
    * Helper method to add a parameter value to an existing
    * assembly parameter map.
    * @param assemblyParams the existing assembly parameter
    * map. Cannot be <code>null</code>.
    * @param name the name of the parameter. Cannot be <code>null</code>
    * or empty.
    * @param value the value in the form of a single string or a
    * string array or multiple strings seperated by
    * commas.
    */
   protected static void addAssemblyParam(Map<String, String[]> assemblyParams,
            String name, String... value)
   {
      if(assemblyParams == null)
         throw new IllegalArgumentException("assemblyParams cannot be null.");
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty.");
      assemblyParams.put(name, value);
   }
   
   /**
    * Helper method to assemble the object specified in
    * the passed in assembly parameters.
    * @param assemblyParams cannot be <code>null</code> or
    * empty.
    * @return a pair object containing the assembly item and
    * result.
    * @throws Exception upon any assembly error that occurs.
    */
   protected static PSPair<IPSAssemblyItem, IPSAssemblyResult> assemble(
            Map<String, String[]> assemblyParams) throws Exception
   {
      if(assemblyParams == null)
         throw new IllegalArgumentException("assemblyParams cannot be null.");
      if(assemblyParams.isEmpty())
         throw new IllegalArgumentException("assemblyParams cannot be empty.");
      IPSAssemblyService aService = PSAssemblyServiceLocator
      .getAssemblyService();
      List<IPSAssemblyItem> aItems;
      IPSAssemblyItem aItem = aService.createAssemblyItem();
      aItem.setParameters(assemblyParams);
      aItem.setUserName(aItem.getParameterValue(IPSHtmlParameters.SYS_USER,null));
      aItem.normalize();
      aItems = Collections.singletonList(aItem);
      List<IPSAssemblyResult> result = aService.assemble(aItems);
      return new PSPair<IPSAssemblyItem, IPSAssemblyResult>(aItem, result.get(0));
   }
   
   /**
    * Helper method to load a slot.
    * @param slotid cannot be <code>null</code> or empty.
    * @return the template slot object, never <code>null</code>.
    * @throws PSNotFoundException if the slot does not exist.
    */
   protected static IPSTemplateSlot loadSlot(String slotid)
           throws PSNotFoundException, PSAssemblyException {
      if(StringUtils.isBlank(slotid))
         throw new IllegalArgumentException("slotid cannot be null or empty.");
      long sid = -1L;
      try
      {
         sid = Long.parseLong(slotid);
      }
      catch(NumberFormatException e)
      {
         throw new IllegalArgumentException("slotid must be a valid long number.");
      }
      IPSAssemblyService aService = PSAssemblyServiceLocator
      .getAssemblyService();
      return aService.loadSlot(new PSGuid(PSTypeEnum.SLOT, sid));
   }
   
   /**
    * Helper method to return the content within the
    * HTML body tags of an assembly result.
    * 
    * @param result the assembly result, cannot be <code>null</code>.
    * @return the body content string, never <code>null</code>.
    * May be empty.
    * @throws Exception
    */
   protected static String getBodyContent(IPSAssemblyResult result) 
   throws Exception
   {
      byte[] res = result.getResultData();
      String resStr = new String(res, "UTF-8");
      Source source = new Source(resStr);
      if (!log.isDebugEnabled())
      {
         // disable output of jericho parse errors unless we turn on debugging for this class
         source.setLogger(null);
      }
     
      Element body = source.getFirstElement(HTMLElementName.BODY);
      
      if (!StringUtils.equalsIgnoreCase(PSServer.getProperty("allowTrueInlineTemplates"), "false"))
      {
         String wrapper = (containsBlockTag(body)) ? "div" : "span";
         return "<"+wrapper+">"+body.getContent().toString()+"</"+wrapper+">";
      }

      return body.getContent().toString();
   }

   
   private static boolean containsBlockTag(Element body)
   {
      List<StartTag> tags = body.getAllStartTags(StartTagType.NORMAL);
      for (Tag tag : tags) {
         if (HTMLElements.getBlockLevelElementNames().contains(tag.getName().toLowerCase())) {
            return true;
         }
      }
      return false;
   }

   public static Set<IPSGuid> getAllowedContentIdsForSlot(String slotid) throws PSAssemblyException, PSNotFoundException {
      IPSTemplateSlot slotObj = loadSlot(slotid);
      Collection<PSPair<IPSGuid, IPSGuid>> assoc = slotObj.getSlotAssociations();
      Set<IPSGuid> ids = new HashSet<IPSGuid>();
      for (PSPair<IPSGuid, IPSGuid> pair : assoc)
      {
    	  ids.add(pair.getFirst());  
      }
      return ids;
   }

   /**
    * Helper method to obtain Allowed node definitions (content types) for a
    * given slot.
    * 
    * @param slotid slot ID string, must not be <code>null</code> or empty and
    * must be a valid slot.
    * @return list of node definitions, never <code>null</code> may be empty.
    * Sorted by names of the definitions.
    */
   @SuppressWarnings("unchecked")
   public static List<IPSNodeDefinition> getAllowedNodeDefsForSlot(
      String slotid) throws PSAssemblyException, PSNotFoundException {
      List<IPSNodeDefinition> defs = new ArrayList<IPSNodeDefinition>();
   
      Collection<IPSGuid> ids = getAllowedContentIdsForSlot(slotid);
   
      List<IPSGuid> processed = new ArrayList<IPSGuid>();
      for (IPSGuid id: ids)
      {
         PSNodeDefinition def = PSContentTypeHelper.findNodeDef(id);
         defs.add(def);
      }
      Collections.sort(defs, new Comparator()
      {
         public int compare(Object obj1, Object obj2)
         {
            IPSNodeDefinition temp1 = (IPSNodeDefinition) obj1;
            IPSNodeDefinition temp2 = (IPSNodeDefinition) obj2;
   
            return temp1.getName().compareTo(temp2.getName());
         }
      });
      return defs;
   }
   
   /**
    * Helper that resolves the supplied site name object and folder path objects
    * following these rules.
    * <p>
    * <ol>
    * <li>If site name object is <code>null</code> then the
    * {@link IPSHtmlParameters#SYS_SITEID} value in the returned map is set to
    * <code>null</code> even if the folder path resolves to a site</li>
    * <li>If the folder path object is <code>null</code> then
    * {@link IPSHtmlParameters#SYS_FOLDERID} value in the returned map is set to
    * <code>null</code> no matter what happens in the next steps.</li>
    * <li>If the folder path starts with "//" then it must start with "//Sites"
    * else it is an error. In this case the site name supplied is resolved to
    * its siteid and the folder path is resolved to its folderid equivalent.</li>
    * <li>If the folder path starts with "/" then the part between the first
    * "/" and next "/" (or till end if there is no second '/') is treated as
    * site name. In this case the site name object value is ignored.</li>
    * <li>The rest (if any) is treated as the path that is appendable to the
    * site's root folde path</li>
    * <li>The folder id for the absolute path thus obtained from the previous
    * step is returned as {@link IPSHtmlParameters#SYS_FOLDERID} in the map</li>
    * </ol>
    * 
    * @param siteNameObj site name as object, may be <code>null</code> in
    * which case we return <code>null</code> for
    * {@link IPSHtmlParameters#SYS_SITEID} in the map.
    * @param folderPathObj folder path as object, may be <code>null</code> in
    * which case we return <code>null</code> for
    * {@link IPSHtmlParameters#SYS_FOLDERID} in the map.
    * @return map of {@link IPSHtmlParameters#SYS_SITEID} and
    * {@link IPSHtmlParameters#SYS_FOLDERID} with <code>null</code> or non
    * <code>null</code> values.
    * @throws PSSiteManagerException
    * @throws PSErrorException
    */
   public static Map<String, IPSGuid> resolveSiteFolders(Object siteNameObj,
      Object folderPathObj) throws PSSiteManagerException, PSErrorException, PSNotFoundException {
      String siteName = siteNameObj == null ? "" : siteNameObj.toString();
      String folderPath = folderPathObj == null ? "" : folderPathObj.toString();
      
      IPSGuid siteid = null;
      IPSGuid folderid = null;
      
      Map<String, IPSGuid> map = new HashMap<String, IPSGuid>();
      // Initialize with null values
      map.put(IPSHtmlParameters.SYS_SITEID, null);
      map.put(IPSHtmlParameters.SYS_FOLDERID, null);

      if (folderPath.startsWith("//") && !folderPath.startsWith("//Sites"))
      {
         throw new IllegalArgumentException(
            "Absolte site folder path must start with '//Sites'");
      }
      if (!StringUtils.isBlank(folderPath) && !folderPath.startsWith("/"))
      {
         throw new IllegalArgumentException(
            "Site folder path must start with '/'");
      }
      
      if(!folderPath.startsWith("//Sites") && folderPath.startsWith("/"))
      {
         StringBuffer absPath = new StringBuffer();
         IPSSite site = PSContentBrowser.computeAbsoluteFolderPath(folderPath,
            absPath);
         siteid = site.getGUID();
         siteName = site.getName();
         folderPath = absPath.toString();
      }

      if (!StringUtils.isBlank(siteName) && siteid == null)
      {
         IPSSiteManager mgr = PSSiteManagerLocator.getSiteManager();
         IPSSite site = mgr.loadSite(siteName);
         siteid = site.getGUID();
      }

      if (!StringUtils.isBlank(folderPath))
      {
         IPSContentWs cws = PSContentWsLocator.getContentWebservice();
         List<IPSGuid> guids = cws.findPathIds(folderPath);
         if (guids.size() < 1)
         {
            throw new IllegalArgumentException("'" + folderPath
               + "' does not resolve to a folder in the system");
         }
         folderid = guids.get(guids.size() - 1);
      }
      // Explicit siteid is required
      if (siteNameObj != null)
         map.put(IPSHtmlParameters.SYS_SITEID, siteid);
      // Explicit fodlerid is required
      if (folderPathObj != null)
         map.put(IPSHtmlParameters.SYS_FOLDERID, folderid);

      return map;
   }

   
}
