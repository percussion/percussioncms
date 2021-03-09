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

package com.percussion.extensions.ca;

import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipDbProcessor;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * This extension builds a content item list for deletion by the Rhythmyx
 * update resource after deleting data from the content type specific tables by
 * cmaking internal requests to the content editor's purge resource. If the
 * attempt to delete this data fails for any reason we add this item to skipped
 * item list for deletion. The DTD for the document is:
 * &lt;!ELEMENT deleterows (row*, skipped) &gt;
 * &lt;!ELEMENT row (#PCDATA) &gt;
 * &lt;!ATTLIST row pkey CDATA #IMPLIED &gt;
 * &lt;!ATTLIST row rid CDATA #IMPLIED &gt;
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
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      if(request == null)
         return; //should never happen

      String appRoot = request.getRequestRoot();
      int loc = appRoot.lastIndexOf("/");
      if(loc > 0)
         appRoot = appRoot.substring(loc+1);   //keep only the application name

      String paramKeyName = "";
      if(params != null && params.length > 0)
         paramKeyName = params[0].toString().trim();

      if(paramKeyName.length() < 1)
         paramKeyName = DEAFULT_PARAM_PURGEURL;

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
         ArrayList purgeurllist = null;
         if(obj instanceof ArrayList)
         {
            purgeurllist = (ArrayList)obj;
         }
         else
         {
            purgeurllist = new ArrayList();
            purgeurllist.add(obj);
         }
         String appResource = "";
         Element parent = null;
         IPSInternalRequest iReq = null;
         String error = null;
         for(int i=0; purgeurllist != null && i<purgeurllist.size(); i++)
         {
            obj = purgeurllist.get(i);
            if(obj == null)
               continue;
            purgeurl = obj.toString().trim();
            appResource = getPurgeAppResource(purgeurl);
            contentid = parseForContentid(purgeurl);
            parent = root;
            error = null;
            if(contentid.length() < 1 || appResource.length() < 1)
            {
               parent = skipped;
               error = "contentid or delete resource app name is empty";
            }
            else
            {
               try
               {
                  request.setParameter(
                     IPSHtmlParameters.SYS_CONTENTID, contentid);
                  request.setParameter(
                     "DBActionType", "DELETE");
                  iReq = request.getInternalRequest(appResource);
                  if(iReq == null)
                  {
                     throw new Exception(
                        "Internal purge request:" + appResource +
                        " does not exist");
                  }
                  iReq.makeRequest();
               }
               catch(Exception e)
               {
                  parent = skipped;
                  error = e.getMessage();
               }
               finally
               {
                  if(iReq != null)
                     iReq.cleanUp();
               }
            }
            elem = PSXmlDocumentBuilder.addElement(doc, parent, ELEM_ROW, null);
            elem.setAttribute(ATTR_PKEY, contentid);
            if(error != null)
            {
               elem.setAttribute(ATTR_ERROR, error);
            }
            
            // get all owner relationships for the current content id
            PSRelationshipDbProcessor processor = 
               new PSRelationshipDbProcessor(request);
            PSRelationshipFilter filter = new PSRelationshipFilter();
            int itemId = Integer.parseInt(request
                  .getParameter(IPSHtmlParameters.SYS_CONTENTID));
            filter.setOwnerId(itemId); // disregard owner revision
            // make sure to disable community filtering
            filter.setCommunityFiltering(false);
            Iterator relationships = 
               processor.getRelationships(filter).iterator();
            while (relationships.hasNext())
            {
               PSRelationship relationship = 
                  (PSRelationship) relationships.next();
               elem.setAttribute(ATTR_PKEY, contentid);
               elem = PSXmlDocumentBuilder.addEmptyElement(doc, parent, ELEM_ROW);
               elem.setAttribute(
                  ATTR_RID, Integer.toString(relationship.getId()));
            }
            
            // get all dependent relationships for the current content id
            filter = new PSRelationshipFilter();
            filter.setDependentId(itemId); // disregard dependent revision
            // make sure to disable community filtering
            filter.setCommunityFiltering(false);
            relationships = processor.getRelationships(filter).iterator();
            while (relationships.hasNext())
            {
               PSRelationship relationship = 
                  (PSRelationship) relationships.next();
               elem.setAttribute(ATTR_PKEY, contentid);
               elem = PSXmlDocumentBuilder.addEmptyElement(doc, parent, ELEM_ROW);
               elem.setAttribute(
                  ATTR_RID, Integer.toString(relationship.getId()));
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
            FileWriter fw = null;
            try
            {
               root.setAttribute("time", new Date().toString());
               fw = new FileWriter(appRoot + "/lastpurge.xml");
               fw.write(PSXmlDocumentBuilder.toString(doc));
               fw.flush();
               fw.close();
            }
            catch(Throwable t)
            {
               PSConsole.printMsg("Exit:" + ms_fullExtensionName, t);
            }
            root.removeChild(skipped); //remove the skipped element for safety!
            request.setInputDocument(doc);
            htmlParams.put("DBActionType", "DELETE");
         }
      }
   }

   /**
    * This helper method parses the url to get the content editor application
    * name, appends the purge resource name "/purge" and then returns. It
    * assumes that there is at least one html parameter (contentid) in the url
    * while parsing.
    */
   private static String getPurgeAppResource(String purgeurl)
   {
      String result = "";
      //assumes at least one parameter i.e. contentid is part of the url
      int loc = purgeurl.indexOf('?');
      if(loc < 0)
         return result;
      purgeurl = purgeurl.substring(0, loc);

      loc = purgeurl.lastIndexOf('/');
      if(loc < 0)
         return result;
      purgeurl = purgeurl.substring(0, loc);

      loc = purgeurl.lastIndexOf('/');
      if(loc < 0)
         return result;
      purgeurl = purgeurl.substring(loc+1);

      if(purgeurl.length() > 0)
         result = purgeurl + PURGE_RESOURCE_NAME;
         
      return result;
   }

   /**
    * This helper method parses the url for contentid value
    */
   private static String parseForContentid(String purgeurl)
   {
      String result = "";
      try
      {
         //assumes contentid is part of the url
         int loc = purgeurl.indexOf("contentid");
         if(loc < 0)
         {
            return result;
         }
         purgeurl = purgeurl.substring(loc);

         loc = purgeurl.indexOf('=');
         if(loc < 0)
         {
            return result;
         }
         purgeurl = purgeurl.substring(loc+1);

         loc = purgeurl.indexOf('&');
         if(loc < 0)
         {
            result = purgeurl;
         }
         else
         {
            result = purgeurl.substring(0, loc);
         }
      }
      catch(Throwable t)
      {
         //ignore any parsing errors!
      }
      return result;
   }

   /**
    * main method for testing purpose.
    */
   public static void main(String[] args)
   {
      PSDeleteContent.getPurgeAppResource("http://10.10.10.17:9992/Rhythmyx/xr_ceImage/image.html?sys_command=preview&sys_contentid=78&sys_revision=1");
      PSDeleteContent.parseForContentid("http://10.10.10.17:9992/Rhythmyx/xr_ceImage/image.html?sys_command=preview&sys_contentid=78&sys_revision=1");
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";

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
    * Default html param name for the purge url.
    */
   static private final String DEAFULT_PARAM_PURGEURL = "purgeurl";

}

