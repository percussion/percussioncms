/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.services.assembly.jexl;

import com.percussion.cms.PSSingleValueBuilder;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.IPSJavaPlugin;
import com.percussion.design.objectstore.IPSJavaPluginConfig;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.impl.PSTrackAssemblyError;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.timing.PSStopwatchStack;
import com.percussion.xmldom.PSXmlDomContext;
import com.percussion.xmldom.PSXmlDomUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Various functions for use in the assembly engine
 * 
 * @author dougrand
 */
public class PSAssemblerUtils extends PSJexlUtilBase
{
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(PSAssemblerUtils.class);

   /**
    * helper to assemble the items in a slot
    * 
    * @param item
    * @param slot
    * @param params
    * @return a list of results
    * @throws Throwable
    */
   @IPSJexlMethod(description = "helper to assemble the items in a slot", params =
   {
         @IPSJexlParam(name = "item", description = "the parent assembly item"),
         @IPSJexlParam(name = "slot", description = "the slot"),
         @IPSJexlParam(name = "params", description = "extra parameters to the process")}, returns = "list of assembly results")
   public List<IPSAssemblyResult> assemble(IPSAssemblyItem item,
         IPSTemplateSlot slot, Map<String, Object> params) throws Throwable
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getCanonicalName() + "#assemble");
      try
      {
         if (slot == null)
         {
            throw new IllegalArgumentException(
                  "slot may not be null, check template's slot reference");
         }
         if (params == null)
            params = new HashMap<>();
         IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         // Handle old slots
         String findername = slot.getFinderName();
         if (findername == null)
         {
            log.warn("No finder defined for slot " + slot.getName()
                  + " defaulting to sys_RelationshipContentFinder");
            findername = "Java/global/percussion/slotcontentfinder/sys_RelationshipContentFinder";
         }
         IPSSlotContentFinder finder = asm.loadFinder(findername);
         if (finder == null)
            throw new PSAssemblyException(IPSAssemblyErrors.MISSING_FINDER,
                  finder);
         if (!StringUtils.isBlank(item.getUserName()))
         {
            // Need user name for preview filter rule
            params.put(IPSHtmlParameters.SYS_USER, item.getUserName());
         } 
         List<IPSAssemblyItem> relitems = finder.find(item, slot, params);
         return asm.assemble(relitems);
      }
      catch (Exception ae) {
         log.error("Assembly Error", ae);

         PSTrackAssemblyError.addProblem(PSI18nUtils
                 .getString("psx_assembly@Error processing slot"), ae);
         // Create clone for response
         IPSAssemblyItem work = (IPSAssemblyItem) item.clone();
         work.setStatus(Status.FAILURE);
         work.setMimeType("text/html");
         StringBuilder results = new StringBuilder();
         results.append("<html><head></head><body>");
         results
                 .append("<div style=\"border: 2px solid red; background-color: #FFEEEE; width:100%; padding:5px; margin:1px; \">");
         results.append("<h2>");
         results.append(PSI18nUtils
                 .getString("psx_assembly@Error processing slot"));
         results.append(" \"");
         if(slot!=null) {
            results.append(slot.getName());
         }
         results.append(" \"");
         results.append("</h2><p>");
         results.append(ae.toString());
         results.append("</p></div></body></html>");
         work.setResultData(results.toString().getBytes(StandardCharsets.UTF_8));
         List<IPSAssemblyResult> rval = new ArrayList<>();
         rval.add((IPSAssemblyResult) work);
         return rval;
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * helper to assemble the items in a slot
    * 
    * @param item
    * @param node
    * @param childname
    * @param templatename
    * @return a list of assembly results
    * @throws Exception
    */
   @IPSJexlMethod(description = "helper to assemble the items in a slot", params =
   {
         @IPSJexlParam(name = "item", description = "the parent assembly item"),
         @IPSJexlParam(name = "node", description = "the node to use for child lookup"),
         @IPSJexlParam(name = "childname", description = "the name of the child field in the item"),
         @IPSJexlParam(name = "templatename", description = "the name of the template to use when formatting the children")}, returns = "the list of assembly results")
   public List<IPSAssemblyResult> assembleChildren(IPSAssemblyItem item,
         Node node, String childname, String templatename) throws Exception
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getCanonicalName() + "#extractBody");
      try
      {
         IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         NodeIterator niter = node.getNodes(childname);
         List<IPSAssemblyItem> items = new ArrayList<IPSAssemblyItem>();
         IPSAssemblyTemplate template = asm.findTemplateByName(templatename);
         while (niter.hasNext())
         {
            IPSAssemblyItem child = (IPSAssemblyItem) item.clone();
            child.setNode(niter.nextNode());
            child.setTemplate(template);
            items.add(child);
         }
         return asm.assemble(items);
      }
      catch (Exception e)
      {
         log.error("Problem during assemble", e);
         throw new Exception(e);
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * helper to assemble the items in a slot
    * 
    * @param slot
    * @return <code>true</code> if the slot is a relationship slot
    * @throws PSAssemblyException
    */
   @IPSJexlMethod(description = "helper to assemble the items in a slot", params =
   {@IPSJexlParam(name = "slot", description = "the slot")}, returns = "true if the passed name refers to an active assembly slot")
   public boolean isAASlot(IPSTemplateSlot slot) throws PSAssemblyException
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      if (slot == null || slot.getFinderName() == null)
         return true;
      IPSSlotContentFinder finder = asm.loadFinder(slot.getFinderName());
      return finder.getType().equals(IPSSlotContentFinder.Type.ACTIVE_ASSEMBLY);
   }

   /**
    * helper to get the popup menu for a content item
    * 
    * @param item
    * @param mode
    * @param context
    * @param name
    * @return gets the popup menu for the given item. This menu is html and
    *         javascript code.
    * @throws UnsupportedEncodingException
    * @throws IOException
    */
   @IPSJexlMethod(description = "helper to get the popup menu for a content item", params =
   {
         @IPSJexlParam(name = "item", type = "IPSGuid", description = "the guid of the item"),
         @IPSJexlParam(name = "mode", description = "the ui mode to query the menu for"),
         @IPSJexlParam(name = "context", description = "the ui context to query the menu for"),
         @IPSJexlParam(name = "name", description = "name for the menu, defaults to the content id if omitted")}, returns = "the source for an action menu link for the given item")
   public String getPopupMenu(IPSGuid item, String mode, String context,
         String name) throws UnsupportedEncodingException, IOException
   {
      return getPopupMenu(item, 0, null, mode, context, name);
   }

   /**
    * helper to get the popup menu for a content item
    * 
    * @param item
    * @param folderid
    * @param siteid
    * @param mode
    * @param context
    * @param name
    * @return gets the popup menu for the given item. This menu is html and
    *         javascript code.
    * @throws UnsupportedEncodingException
    * @throws IOException
    */
   @IPSJexlMethod(description = "helper to get the popup menu for a content item", params =
   {
         @IPSJexlParam(name = "item", type = "IPSGuid", description = "the guid of the item"),
         @IPSJexlParam(name = "folderid", type = "int", description = "the folder id, or 0 for none"),
         @IPSJexlParam(name = "siteid", type = "IPSGuid", description = "the site id, or 0 for none"),
         @IPSJexlParam(name = "mode", description = "the ui mode to query the menu for"),
         @IPSJexlParam(name = "context", description = "the ui context to query the menu for"),
         @IPSJexlParam(name = "name", description = "name for the menu, defaults to the content id if omitted")}, returns = "the source for an action menu link for the given item")
   public String getPopupMenu(IPSGuid item, int folderid, IPSGuid siteid,
         String mode, String context, String name)
         throws UnsupportedEncodingException, IOException
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getCanonicalName() + "#getPopupMenu");
      try
      {
         if (item == null)
         {
            throw new IllegalArgumentException("item may not be null");
         }
         if (StringUtils.isBlank(mode))
         {
            throw new IllegalArgumentException("mode may not be null or empty");
         }
         if (StringUtils.isBlank(context))
         {
            throw new IllegalArgumentException(
                  "context may not be null or empty");
         }

         PSLegacyGuid lg = (PSLegacyGuid) item;

         PSRequest req = (PSRequest) PSRequestInfo
               .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
         Map<String, Object> extraParams = new HashMap<String, Object>();
         extraParams.put("sys_contentid", lg.getContentId());
         if (siteid != null)
         {
            extraParams.put("sys_siteid", siteid.longValue());
         }
         if (folderid != 0)
         {
            extraParams.put("sys_folderid", folderid);
         }
         extraParams.put("omitlink", "true");
         extraParams.put("sys_mode", mode);
         extraParams.put("sys_uicontext", context);
         if (!StringUtils.isBlank(name))
         {
            extraParams.put("menuname", name);
         }

         PSInternalRequest ireq = PSServer.getInternalRequest(
               "/sys_PortalSupport/ActionMenu.html", req, extraParams, false);
         ByteArrayOutputStream os = null;
         try
         {
            os = ireq.getMergedResult();
            return new String(os.toByteArray());
         }
         catch (PSInternalRequestCallException e)
         {
            log.error("Problem getting popup", e);
         }
         finally
         {
            if (os != null)
               os.close();
         }

         return "Broken popup";
      }
      finally
      {
         sws.stop();
      }
   }

   @IPSJexlMethod(description = "typically used to prepare the fields to be xml compliant", params =
   {
         @IPSJexlParam(name = "content", description = "string content that needs to be tidied")}, returns = "tidied content")

   public String getTidiedContent(String content) throws Exception
   {
      if (StringUtils.isBlank(content))
      {
         return "";
      }
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSXmlDomContext contxt = new PSXmlDomContext(PSAssemblerUtils.class
            .getName(), new PSRequestContext(req));
    //  contxt.setRxCommentHandling(true);
      contxt.setTidyProperties("rxW2Ktidy.properties");
      contxt.setServerPageTags("rxW2KserverPageTags.xml");
      contxt.setUsePrettyPrint(false);
      content = PSXmlDomUtils.tidyInput(contxt, content);
      // Tidy makes the incoming content as html document, get the content from
      //body tag and return it.
      String lcContent = content.toLowerCase();
      if (StringUtils.contains(lcContent, "<body")
            && StringUtils.contains(lcContent, "</body>"))
      {
         content = StringUtils.substring(content, lcContent.indexOf("<body"));
         content = StringUtils.substring(content, content.indexOf(">") + 1);
         content = StringUtils.substring(content, 0, content.toLowerCase()
               .lastIndexOf("</body>"));
      }
      return content;
   }

   /**
    * helper setup the variables needed to invoke an applet
    * 
    * @param request
    * @param data
    */
   @IPSJexlMethod(description = "helper setup the variables needed to invoke an applet", params =
   {
         @IPSJexlParam(name = "request", type = "HttpServletRequest", description = "the servlet request, never null"),
         @IPSJexlParam(name = "data", description = "a string-string map which will be modified with the addition applet data (CLASSID, CODEBASE, PLUGIN_VERSION, VERSION_TYPE)")})
   public void addAppletConfig(HttpServletRequest request,
         Map<String, String> data)
   {
      PSServerConfiguration config = PSServer.getServerConfiguration();
      IPSJavaPluginConfig pluginCfg = config.getJavaPluginConfig();

      // Get the plugin by passing the userAgent
      IPSJavaPlugin plugin = pluginCfg.getPlugin(request
            .getHeader("user-agent"));

      // If the plugin is null then get the default plugin
      if (plugin == null)
      {
         plugin = pluginCfg.getDefaultPlugin();
      }

      String classid = "";
      String version_type = "";

      if (plugin.isStaticVersioning())
      {
         classid = plugin.getStaticClsid();
         version_type = IPSJavaPlugin.VERSION_STATIC;
      }
      else
      {
         classid = IPSJavaPlugin.CLASSID_DYNAMIC;
         version_type = IPSJavaPlugin.VERSION_DYNAMIC;
      }

      data.put("CLASSID", classid);
      data.put("CODEBASE", plugin.getDownloadLocation());
      data.put("PLUGIN_VERSION", plugin.getVersionToUse());
      data.put("VERSION_TYPE", version_type);
   }

   /**
    * Get the title
    * 
    * @param guid
    * @return the title of the given content item. Returns the guid if there is
    *         no name.
    */
   @IPSJexlMethod(description = "helper to get the title for a content item", params =
   {@IPSJexlParam(name = "item", type = "IPSGuid", description = "the guid of the item")}, returns = "the title for the passed guid")
   public String getTitle(IPSGuid guid)
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getCanonicalName() + "#getTitle");
      try
      {
         PSLegacyGuid lg = (PSLegacyGuid) guid;
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary sum = cms.loadComponentSummary(lg.getContentId());
         return sum != null ? sum.getName() : guid.toString();
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * helper to extract a single parameter value
    * 
    * @param params
    * @param name
    * @return the param value, may be <code>null</code>
    */
   @IPSJexlMethod(description = "helper to extract a single parameter value", params =
   {
         @IPSJexlParam(name = "params", type = "Map<String,String[]>", description = "the request parameters as accessed from $sys.params"),
         @IPSJexlParam(name = "name", type = "String", description = "the desired parameter name")}, returns = "the first defined value for the parameter or null if the parameter isn't present")
   public String getSingleParamValue(Map<String, String[]> params, String name)
   {
      String values[] = params.get(name);
      if (values != null && values.length > 0)
      {
         return values[0];
      }
      else
      {
         return null;
      }
   }

   /**
    * Combine parameter values
    * 
    * @param input
    * @param urlquery
    * @return a map with the combined values
    */
   @SuppressWarnings("unchecked")
   @IPSJexlMethod(description = "helper to combine new parameter values", params =
   {
         @IPSJexlParam(name = "params", type = "Map<String,String[]>", description = "the request parameters as accessed from $sys.params"),
         @IPSJexlParam(name = "urlquery", type = "String", description = "a url query style string with names and values separated by equals, and these pairs separated by ampersands")}, returns = "the output is a new map where values specified in the urlquery replace those from the input")
   public Map<String, Object> combine(Map<String, String[]> input,
         String urlquery)
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getCanonicalName() + "#combine");
      try
      {
         // Parse the query
         String pairs[] = urlquery.split("&");
         Map<String, Object> urlmap = new HashMap<String, Object>();
         for (String pair : pairs)
         {
            String parts[] = pair.split("=");
            if (parts.length == 2)
            {
               String key = parts[0];
               List<String> value = (List<String>) urlmap.get(key);
               if (value == null)
               {
                  value = new ArrayList<String>();
                  urlmap.put(key, value);
               }
               value.add(parts[1]);
            }
            else if (StringUtils.isNotBlank(pair))
            {
               throw new IllegalArgumentException("This value '" + pair
                     + "' is badly formed for a url query parameter, "
                     + "which must have the form param=value");
            }
         }

         // Walk the map and convert to array elements
         for (String key : urlmap.keySet())
         {
            List<String> value = (List<String>) urlmap.get(key);
            String arr[] = new String[value.size()];
            value.toArray(arr);
            urlmap.put(key, arr);
         }

         Map<String, Object> casted = urlmap;
         return combine(input, casted);
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * Combine parameter values
    * 
    * @param input
    * @param extra
    * @return a map combining parameter values
    */
   @SuppressWarnings("unchecked")
   @IPSJexlMethod(description = "helper to combine new parameter values", params =
   {
         @IPSJexlParam(name = "params", type = "Map<String,String[]>", description = "the request parameters as accessed from $sys.params"),
         @IPSJexlParam(name = "extra", type = "Map<String,Object>", description = "a second parameter map")}, returns = "the output is a new map where values specified in the urlquery replace those from the input")
   public Map<String, Object> combine(Map<String, String[]> input,
         Map<String, Object> extra)
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getCanonicalName() + "#combine");
      try
      {
         Map<String, Object> rval = new HashMap<String, Object>();
         Set<String> keys = new HashSet<String>();
         keys.addAll(input.keySet());
         keys.addAll(extra.keySet());

         for (String key : keys)
         {
            String first[] = input.get(key);
            Object second = extra.get(key);

            if (second != null)
            {
               rval.put(key, second);
            }
            else if (first != null)
            {
               rval.put(key, first);
            }
            else
            {
               log.error("Found no value for key: \"" + key + "\"");
            }
         }

         return rval;
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * extract a specific property of each child via the named child node list
    * 
    * @param parentNode
    * @param childName
    * @param propertyName
    * @return a list of jsr-170 property values. There will be one value per
    *         child.
    * @throws RepositoryException
    */
   @IPSJexlMethod(description = "extract a specific property of each child via the named child node list", params =
   {
         @IPSJexlParam(name = "parentNode", type = "javax.jcr.Node", description = "the parent node to get the child nodes from, usually $sys.item"),
         @IPSJexlParam(name = "childName", description = "the name of the child node"),
         @IPSJexlParam(name = "propertyName", description = "the name of the property on the child")}, returns = "a list of the matching values")
   public List<Object> childValues(Node parentNode, String childName,
         String propertyName) throws RepositoryException
   {
      if (parentNode == null)
      {
         throw new IllegalArgumentException("parentNode may not be null");
      }
      if (StringUtils.isBlank(childName))
      {
         throw new IllegalArgumentException(
               "childName may not be null or empty");
      }
      if (StringUtils.isBlank(propertyName))
      {
         throw new IllegalArgumentException(
               "propertyName may not be null or empty");
      }
      List<Object> rval = new ArrayList<Object>();

      NodeIterator niter = parentNode.getNodes(childName);
      while (niter.hasNext())
      {
         Node child = niter.nextNode();
         Property prop = child.getProperty(propertyName);
         rval.add(prop.getValue());
      }

      return rval;

   }

   /**
    * extract a specific value of each map via the named key in the list
    * 
    * @param maplist
    * @param key
    * @return a list of string values
    * @throws RepositoryException
    */
   @IPSJexlMethod(description = "extract a specific value of each map via the named key in the list", params =
   {
         @IPSJexlParam(name = "maplist", type = "java.util.List<java.util.Map<String,Object>>", description = "Each element in the list is a map. Each map associates strings to objects"),
         @IPSJexlParam(name = "key", description = "the name of the child node")}, returns = "a list of the string extraction of the objects")
   public List<String> mapValues(List<Map<String, Object>> maplist, String key)
         throws RepositoryException
   {
      if (maplist == null)
      {
         throw new IllegalArgumentException("maplist may not be null");
      }
      if (StringUtils.isBlank(key))
      {
         throw new IllegalArgumentException("key may not be null or empty");
      }
      List<String> rval = new ArrayList<String>();

      for (Map<String, Object> map : maplist)
      {
         Object val = map.get(key);
         if (val == null)
         {
            rval.add("");
         }
         else
         {
            rval.add(val.toString());
         }
      }

      return rval;

   }

   /**
    * Get the assembly items that go into the supplied slot of the supplied
    * parent item.
    * 
    * @param item parent item, must not be <code>null</code>.
    * @param slot slot to load snippets for, must not be <code>null</code>/
    * @param params additional parameters, may be <code>null</code> or empty.
    * @return an array of zero or more ordered related slot items, never
    * <code>null</code>, but may be empty
    * @throws PSAssemblyException
    * @throws PSFilterException
    * @throws RepositoryException
    */
   public List<IPSAssemblyItem> getSlotItems(IPSAssemblyItem item,
      IPSTemplateSlot slot, Map<String, Object> params)
           throws PSAssemblyException, PSFilterException, RepositoryException, PSNotFoundException {
      if (slot == null || slot.getFinderName() == null)
         throw new IllegalArgumentException(
            "slot and slot finder must not be null");
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSSlotContentFinder finder = asm.loadFinder(slot.getFinderName());
      if (!StringUtils.isBlank(item.getUserName()))
      {
         // Create an new map, so that we don't pollute the original one.
         params = new HashMap<>(params);
         // Need user name for preview filter rule
         params.put(IPSHtmlParameters.SYS_USER, item.getUserName());
      }
      return finder.find(item, slot, params);
   }
   
   /**
    * Gets error message when failed to assemble the specified item.
    * @param item the item, assumed not <code>null</code>.
    * @return the error message, not blank.
    */
   private String getErrorMsgForItemSlot(IPSAssemblyItem item, IPSTemplateSlot slot)
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = cms.loadComponentSummary(item.getId().getUUID());
      
      return "Problem assembling slot "+slot.getName()+" in item (name=\"" + summary.getName()
            + "\", id=" + item.getId().toString() + ") with template: "
            + item.getTemplate().getName() + ".";
   }
   
   @IPSJexlMethod(description = "helper to get selected text if available", params =
   {@IPSJexlParam(name = "params", description = "the html params eg. $sys.params"),
         @IPSJexlParam(name = "defaultText", description = "The text to use if selected text is not available")}, returns = "The text String or default text if not available")
   public String getSelectedText(Map<String, String[]> params, String defaultText) throws Throwable
   {
      String result = defaultText;
      String[] selectedTextList = params.get(PSSingleValueBuilder.INLINE_TEXT);
      if (selectedTextList != null && selectedTextList.length > 0)
      {
         String selectedText = selectedTextList[0];
         if (StringUtils.isNotEmpty(selectedText))
         {
            result = PSSingleValueBuilder.decodeSelectedText(selectedText);
         }
      }
      return result;
   }

   @IPSJexlMethod(description = "helper to get anchor text if available.  $sys.anchortext can be used in location scheme "
         + " but this is useful for external links etc.", params =
   {@IPSJexlParam(name = "params", description = "the html params eg. $sys.params"),
         @IPSJexlParam(name = "addHash", description = "add the # before the string if anchor available.")}, returns = "The text String or empty string if not available")
   public String getAnchor(Map<String, String[]> params, boolean addHash) throws Throwable
   {
      String result = "";
      String[] anchorTextList = params.get(PSSingleValueBuilder.ANCHOR_TEXT);
      if (anchorTextList != null && anchorTextList.length > 0)
      {
         String anchorText = anchorTextList[0];
         if (StringUtils.isNotEmpty(anchorText))
         {
            if (addHash)
               result += "#";
            result += anchorText;
         }
      }
      return result;
   }
   
 
   @IPSJexlMethod(description = "import a file into the assembled document", params =
   {
            @IPSJexlParam(name = "path", type = "String", description = "The path to the include")},
            returns = "The file contents as a string")
   public String importFile(String path)
   { 
      File file = new File(path);
      String fileString="";
      try
      {
         fileString = FileUtils.readFileToString(file);
      }
      catch (IOException e)
      {
         log.error("Cannot find file for assembly import : "+ path);
      } 
      return fileString;
   }
   
   @IPSJexlMethod(description = "import an include file separated with the includeStart and includeEnd velocity macros and convert to a map", params =
      {
               @IPSJexlParam(name = "path", type = "String", description = "The path to the include")},
               returns = "a map where the key is the value passed to includeStart")
   public Map<String,String> readMultiIncludeFile(String path)
   {
      Map<String,String> includeMap = new HashMap<String,String>();
    
      if (StringUtils.isEmpty(path))
      {
        
         includeMap.put("INCLUDE_ERROR", "Empty include file path");
         return includeMap;
      }
      
      FileInputStream fis = null;
      BufferedReader br = null;
      try 
      {
         fis = new FileInputStream(new File(path));
         br = new BufferedReader(new InputStreamReader(fis,"UTF-8"));
       
         String line = null;
         String key = null;
         StringBuilder snippet = null;
         long size = 0;
         while ((line = br.readLine()) != null) {
            size += line.length();
            if (size > 50000000)
               throw new IllegalArgumentException("Include file "+path+" is too large Max size 50M characters");
            
            String INCLUDE_SEPARATOR = "----------------------------- include";
            
            if (line.contains(INCLUDE_SEPARATOR))
            {
               String[] sepinfo = line.trim().split(" ");
               if (sepinfo.length != 4)
                  throw new IllegalArgumentException("Invalid include separator "+line);
              
               String name = sepinfo[3];
               if (sepinfo[2].equals("start"))
               {
                  key = name;
                  snippet = new StringBuilder();
               } else if (sepinfo[2].equals("end") && snippet != null)
               {
                  includeMap.put(key, snippet.toString());
                  key = null;
               } else
               {
                  throw new IllegalArgumentException("Invalid command "+sepinfo[2]+ "in  separator "+line);   
               }
               
            } else if (key!=null && snippet != null)
            {
               snippet.append(line);
            }
         }
      }
      catch (FileNotFoundException e)
      {
         includeMap.put("INCLUDE_ERROR","NOT_FOUND: "+ path);
      }
      catch (Exception e)
      {
         log.debug("Cannot find import file: "+path+" may be invalid or not published yet" , e);
         includeMap.put("INCLUDE_ERROR", "Error loading include "+path+" "+ e.getMessage());
      }
      finally
      {
         if (fis!=null) try {fis.close();} catch (Exception e){}
         if (br!=null) try {br.close();} catch (Exception e){}
      }
      
      return includeMap;
   }

   @IPSJexlMethod(description = "Start a performance timer", params =
      {
               @IPSJexlParam(name = "item", type = "IPSAssemblyItem", description = "The $sys.assemblyItem object"),
               @IPSJexlParam(name = "name", type = "String", description = "The name of the counter")},
               returns = "a map where the key is the value passed to includeStart")
   public void timerStart(IPSAssemblyItem item, String name)
   {
      HashMap<String, PSStopwatch> timers = getTimers(item);
      PSStopwatch stopWatch = new PSStopwatch();
      timers.put(name, stopWatch);
      stopWatch.start();
   }
   
   @IPSJexlMethod(description = "Stop a performance timer", params =
      {
               @IPSJexlParam(name = "item", type = "IPSAssemblyItem", description = "The $sys.assemblyItem object"),
               @IPSJexlParam(name = "name", type = "String", description = "The name of the counter")})
   public void timerStop(IPSAssemblyItem item, String name)
   {
      PSStopwatch stopWatch = getTimers(item).get(name);
      if (stopWatch!=null)
      {
         stopWatch.stop();
      }
   }
   
   @IPSJexlMethod(description = "return the count of a performance timer", params =
      {
               @IPSJexlParam(name = "item", type = "IPSAssemblyItem", description = "The $sys.assemblyItem object"),
               @IPSJexlParam(name = "name", type = "String", description = "The name of the counter")},
               returns = "The number of miliseconds")
   public double timerElapsed(IPSAssemblyItem item, String name)
   {
      PSStopwatch stopWatch = getTimers(item).get(name);
   
      return (stopWatch == null) ? 0L : stopWatch.elapsed();
   }
   
   
   @IPSJexlMethod(description = "Returns html showing the current timer times", params =
      {
               @IPSJexlParam(name = "item", type = "IPSAssemblyItem", description = "The $sys.assemblyItem object")},
               returns = "hashmap containing current timer keys and elapsed values")
   public HashMap<String,Double> timerOutput(IPSAssemblyItem item)
   {
      HashMap<String, PSStopwatch> timers = getTimers(item);
      HashMap<String,Double> results = new HashMap<String,Double>();
      StringBuilder sb = new StringBuilder();
      sb.append("<div class=\"perc_timers\">");
      for (Map.Entry<String, PSStopwatch> entry : timers.entrySet())
      {
         results.put(entry.getKey(), entry.getValue().elapsed());
      }
      return results;
   }
   
   @IPSJexlMethod(description = "resets all the performance timers", params =
      {
               @IPSJexlParam(name = "item", type = "IPSAssemblyItem", description = "The $sys.assemblyItem object")})
   public void timerReset(IPSAssemblyItem item)
   {
      HashMap<String, PSStopwatch> timers = getTimers(item);
      timers.clear();
   }
   
   private HashMap<String, PSStopwatch> getTimers(IPSAssemblyItem item)
   {
      HashMap<String,Object> sys = (HashMap<String,Object>)item.getBindings().get("$sys");
      HashMap<String,PSStopwatch> timers = (HashMap<String,PSStopwatch>)sys.get("percTimers");
      if (timers == null)
      {
         timers = new HashMap<String,PSStopwatch>();  
         sys.put("percTimers",timers);
      }

      return timers;
   }
   
   @IPSJexlMethod(description = "logs to the system log file.  Too much logging will slow publishing and fill logs", params =
      {
               @IPSJexlParam(name = "string", type = "String", description = "The string to log"),
               @IPSJexlParam(name = "level", type = "String", description = "INFO, DEBUG, or ERROR")})
   public void log(String string, String level)
   {
      if (level == null)
         level = "INFO";
      
      String lvlUp = level.toUpperCase();
      if(lvlUp.equals("DEBUG")) 
            log.debug(string);
      else if (lvlUp.equals("INFO")) 
            log.info(string);
      else if (lvlUp.equals("ERROR")) 
            log.error(string);
    
   }
   
   @IPSJexlMethod(description = "Returns the username of the logged in user", params = { })
   public String getUserName(){
	    String userName = (String) PSRequestInfo.getRequestInfo(
	            PSRequestInfo.KEY_USER);
	         if (StringUtils.isBlank(userName))
	         {
	            PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
	               PSRequestInfo.KEY_PSREQUEST);
	            if (req != null)
	               userName = req.getUserSession().getRealAuthenticatedUserEntry();
	         }
	   
	         return userName;
   }
   
   @IPSJexlMethod(description = "Returns the Roles of the logged in user", params = { })
   public List<String> getUserRoles(){
	   PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
	               PSRequestInfo.KEY_PSREQUEST);
	   
	   if (req != null)
		   return req.getUserSession().getUserRoles();
	   
	return new ArrayList<String>();

   }
}
