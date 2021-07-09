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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.extensions.cx;

import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.server.cache.IPSCacheHandler;
import com.percussion.server.cache.PSAssemblerCacheHandler;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.cache.PSFolderRelationshipCache;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * This extension builds a content item list for deletion by the Rhythmyx
 * update resource after deleting data from the content type specific tables by
 * cmaking internal requests to the content editor's purge resource. If the
 * attempt to delete this data fails for any reason we add this item to skipped
 * item list for deletion. The DTD for the document is:
 * &lt;!ELEMENT deleterows (row*, skipped) &gt;
 * &lt;!ELEMENT row (#PCDATA) &gt;
 * &lt;!ATTLIST row pkey CDATA #IMPLIED &gt;
 * &lt;!ELEMENT skipped (row*) &gt;
 * This exit shall typically placed on an Rx update resource that deletes the
 * rows from all system tables tables. The XML element pkey must be mapped to
 * the primary key in the backed table(s).
 *
 */
public class PSDeleteContent implements IPSRequestPreProcessor
{
   /*
    * implementation of the method in the interface IPSRequestPreProcessor 
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * implementation of the method in the interface IPSRequestPreProcessor
    * 
    * @param params Expects a single parameter, which is the name of the 
    * request parameter containing the contentid(s) to purge.  The html param
    * may contain a single id or a list of ids.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      if(request == null)
         return; //should never happen

      String paramKeyName = "";
      if(params != null && params.length > 0)
         paramKeyName = params[0].toString().trim();

      if(paramKeyName.length() < 1)
         paramKeyName = IPSHtmlParameters.SYS_CONTENTID;

      String appRoot = request.getRequestRoot();
      int loc = appRoot.lastIndexOf("/");
      if(loc > 0)
         appRoot = appRoot.substring(loc+1);   //keep only the application name


      Map<String,Object> htmlParams = request.getParameters();
      Document doc = null;
      Element root = null;
      Element skipped = null;
      try
      {
         doc = PSXmlDocumentBuilder.createXmlDocument();
         root = PSXmlDocumentBuilder.createRoot(doc, ELEM_DELETEROWS);
         skipped =
            PSXmlDocumentBuilder.addElement(doc, root, ELEM_SKIPPED, null);

         //map is empty nothing to delete!
         if(htmlParams == null)
            return;

         Object obj = htmlParams.get(paramKeyName);

         String purgeurl = null;
         String contentid = null;
         Element elem = null;
         ArrayList purgeContentIdList = null;
         if(obj instanceof ArrayList)
         {
            purgeContentIdList = (ArrayList)obj;
         }
         else
         {
            purgeContentIdList = new ArrayList();
            purgeContentIdList.add(obj.toString());
         }
         String ceResource = "";
         String purgeResource = "";
         Element parent = null;
         IPSInternalRequest iReq = null;
         String error = null;
         for(int i=0; purgeContentIdList != null && i<purgeContentIdList.size(); 
            i++)
         {
            boolean hasPurgeResource = true;
            obj = purgeContentIdList.get(i);
            if(obj == null)
               continue;
            contentid = obj.toString().trim();
            ceResource = getEditorUrl(contentid, request);
            if (ceResource != null)
               purgeResource = getPurgeUrl(ceResource);
            parent = root;
            error = null;
            if(contentid.length() < 1 ||
               ceResource.length() < 1 || 
               purgeResource == null ||
               purgeResource.length() < 1)
            {
               parent = skipped;
               error = "contentid or delete resource app name is empty";
            }
            else
            {
               try
               {
                  // purge the content
                  request.setParameter(
                          IPSHtmlParameters.SYS_CONTENTID, contentid);
                  request.setParameter("DBActionType", "DELETE");
                  request.setParameter("sys_changeEventOnly", "yes");
                  iReq = request.getInternalRequest(purgeResource);
                  // purge using the existing resource
                  // if it does not exist use the auto purge
                  if(iReq != null)
                      iReq.performUpdate();
                  else
                  {
                     hasPurgeResource = false;
                     request.removeParameter("sys_changeEventOnly");
                  }
                  // Check for the modify handler, if it does not exist, throw
                  // an exception, if it exists, first perform delete 
                  // relationships and then notify the handler
                  Map<String,Object> reqParams = request.getParameters();
                  String val = request.getParameter("sys_changeEventOnly");
                  Map tmpParams = new HashMap();
                  tmpParams.put(IPSHtmlParameters.SYS_COMMAND, "modify");
                  tmpParams.put(IPSHtmlParameters.SYS_REVISION, "-1");
                  iReq = request.getInternalRequest(ceResource, tmpParams, 
                     true);
                  if (iReq == null)
                  {
                     throw new Exception("Unable to find resource:" + 
                        ceResource);
                  }
                  deleteRelationships(request);
                  // now perform the delete, so everyone is HAPPY HAPPY
                 
                  iReq.performUpdate();
       
               }
               catch(Exception e)
               {
                  purge_log.debug("Exception deleting relationships for purge id "+ contentid);
                  parent = skipped;
                  error = e.getMessage();
               }
            }
            // Resolved issue CONTENTSTATUSHISTORY and other tables not being
            // cleared
            elem = PSXmlDocumentBuilder.addElement(doc, parent, ELEM_ROW, null);
            elem.setAttribute(ATTR_PKEY, contentid);
            if (error != null)
            {
               elem.setAttribute(ATTR_ERROR, error);
            }
         }
         
      }
      catch(Exception e)
      {
         PSConsole.printMsg("Exit:" + ms_fullExtensionName, e);
      }
      finally
      {
         if(doc != null)
         {
            try
            {
               root.setAttribute("time", new Date().toString());
               IPSRhythmyxInfo rxInfo = PSRhythmyxInfoLocator.getRhythmyxInfo();
               String rxRootDir = (String) rxInfo
                     .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);

               try(FileWriter fw = new FileWriter(rxRootDir + File.separator + appRoot
                       + "/lastpurge.xml")) {
                  fw.write(PSXmlDocumentBuilder.toString(doc));
                  fw.flush();
               }
            }
            catch(Exception t)
            {
               PSConsole.printMsg("Exit:" + ms_fullExtensionName, t);
            }
            root.removeChild(skipped); //remove the skipped element for safety!
            request.setInputDocument(doc);
            if(htmlParams!=null) {
               htmlParams.put("DBActionType", "DELETE");
            }
         }
      }
   }

   /**
    * Updates both folder and assembly caches for the modified relationships.
    * 
    * @param relationships the modified relationships, assumed not 
    *   <code>null</code>.
    */
   private void updateCaches(PSRelationshipSet relationships)
   {
      // update the folder cache if needed
      PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
      if (cache != null)
      {
            cache.delete(relationships);
      }
      
      // update the assembly cache
      PSCacheManager mgr = PSCacheManager.getInstance();
      IPSCacheHandler handler = mgr.getCacheHandler(
         PSAssemblerCacheHandler.HANDLER_TYPE);
         
      // if caching not enabled, handler will be null
      if (! (handler instanceof PSAssemblerCacheHandler))
         return;
      
      PSRelationshipChangeEvent event = new PSRelationshipChangeEvent(
              PSRelationshipChangeEvent.ACTION_REMOVE, relationships);

      ((PSAssemblerCacheHandler)handler).relationshipChanged(event);
   }
   
   /**
    * This helper method parses the url to get the content editor application
    * name, appends the purge resource name "/purge" and then returns. 
    * 
    * @param ceUrl The base content editor url returned by 
    * {@link #getEditorUrl(String, IPSRequestContext)}, assumed not 
    * <code>null</code> or empty.
    * 
    * @return The url to call to purge the item from the editor tables, 
    * <code>null</code> if it cannot be parsed, never empty.
    */
   private String getPurgeUrl(String ceUrl)
   {
      String result = null;
      
      int loc = ceUrl.lastIndexOf('/');
      if(loc < 0)
         return result;
      ceUrl = ceUrl.substring(0, loc);

      loc = ceUrl.lastIndexOf('/');
      if(loc < 0)
         return result;
      ceUrl = ceUrl.substring(loc+1);

      if(ceUrl.length() > 0)
         result = ceUrl + PURGE_RESOURCE_NAME;

      return result;
   }

   /**
    * This helper method retrieves the base url for the appropriate content
    * editor resource 
    * e.g. http://locahost:9992/Rhythmxy/rx_ceArticle/article.html
    * 
    * @param contentid The contentId of the item for which the url is to be
    * retreived.  Assumed not <code>null</code> or empty.
    * @param request The current request context, assumed not <code>null</code>.
    * 
    * @return The url, or <code>null</code> if the url cannot be retrived, never 
    * empty.
    */
   private String getEditorUrl(String contentid, IPSRequestContext request)
   {
      Document doc = null;
      IPSInternalRequest iReq = null;
      try
      {
         request.setParameter(IPSHtmlParameters.SYS_CONTENTID, contentid);
         iReq = request.getInternalRequest("sys_ceSupport/contenteditorurls");
         if (iReq != null)         
            doc = iReq.getResultDoc();
      }
      catch(Exception e)
      {
         // this should not happen, means system is unstable somehow
         throw new RuntimeException("Unable to retrieve url for purge: " + 
            e.getLocalizedMessage());
      }
      
      if(doc == null)
         return null;
      String ceUrl = doc.getDocumentElement().getAttribute("editurl");
      String result = "";
      //assumes at least one parameter i.e. contentid is part of the url
      int loc = ceUrl.indexOf('?');
      if(loc < 0)
         return result;
      result = ceUrl.substring(0, loc);
            
      return result;
   }
   /**
    * Before performing the delete i.e. execute delete plans
    * which would wipe out data from the tables, delete
    * relationships
    * 
    * Changed to use underlying relationship service to prevent action firing
    * which may throw error and prevent relationship from being removed.
    * 
    * @param request The request context
 * @throws PSException 
    */
   private void deleteRelationships(IPSRequestContext request) throws PSException {
      
       final IPSRelationshipService svc = PSRelationshipServiceLocator
               .getRelationshipService();
       // get all owner relationships for the current content id and delete
       PSRelationshipFilter filter = new PSRelationshipFilter();
       filter.setCommunityFiltering(false); // do not filter by community
       int itemId = Integer.parseInt(request
             .getParameter(IPSHtmlParameters.SYS_CONTENTID));
       filter.setOwnerId(itemId); // disregard owner rev     
       final List<PSRelationship> rels = svc.findByFilter(filter);
      
       // get all dependent relationships for the current content id and delete
       filter = new PSRelationshipFilter();
       filter.setCommunityFiltering(false); // do not filter by community
       filter.setDependentId(itemId); // disregard dependent revision
      
       rels.addAll(svc.findByFilter(filter));
       if (rels!=null && !rels.isEmpty()){
           svc.deleteRelationship(rels);
       }
       PSRelationshipSet relset = new PSRelationshipSet();
       relset.addAll(rels);
       updateCaches(relset);
   }
   
   /**
    * The fully qualified name of this extension.
    */
   private String ms_fullExtensionName = "";

   /**
    * Name of the purge URL that must be present in the content editor app. This
    * name is fixed and hence hard coded.
    */
   static private final String PURGE_RESOURCE_NAME = "/purge";

   /**
    * Name of the element 'deleterows'.
    */
   static private final String ELEM_DELETEROWS = "deleterows";

   /**
    * Name of the element 'skipped'.
    */
   static private final String ELEM_SKIPPED = "skipped";

   /**
    * Name of the element 'row'.
    */
   static private final String ELEM_ROW = "row";

   /**
    * Name of the attribute 'error'.
    */
   static private final String ATTR_ERROR = "error";

   /**
    * Name of the attribute 'pkey'.
    */
   static private final String ATTR_PKEY = "pkey";

   /**
    * Name of the attribute primary key for the relationship tables.
    */
   static private final String ATTR_RID = "rid";
   
   /**
    * The log instance to use for purging, never <code>null</code>.
    */
   private static final Logger purge_log = LogManager.getLogger("PurgeLog");

}

