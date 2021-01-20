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
package com.percussion.search;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This exit is used to add thumbnail urls to the thumbnail column (if it exists) for
 * each row in the returned search result set. In order to determine the default variant
 * to use for each image content type, an entry must be put in the
 * sys_AddThumbnailURL.properties file located in rxconfig\Server
 */
public class PSAddThumbnailURL implements
   IPSSearchResultsProcessor
{
   /*
    * @see com.percussion.extension.IPSExtension#init(
    * com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
            throws PSExtensionException
   {
      // no op
   }

   /**
    * Iterates through all search result rows and adds the thumbnail urls to
    * the thumbnail columns if they exist. The thumbnail column to use can be passed
    * in as parameter [0] or it will default to use sys_thumbnail.
    * @see com.percussion.extension.IPSSearchResultsProcessor#processRows(
    * java.lang.Object[],
    *      java.util.List, com.percussion.server.IPSRequestContext)
    */
   public List processRows(Object[] params, List rows, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      // Set the name of the thumbnail column to use
      if(params.length > 0
         && params[0] instanceof String
         && ((String)params[0]).trim().length() > 0)
         m_thumbnail = (String)params[0];
      else
         m_thumbnail = THUMBNAIL;

      Iterator it = rows.iterator();
      String contentTypeId = null;
      String contentId = null;
      String variant = null;
      String revision = null;
      m_variantUrls.clear(); // clear the variant urls cache
      m_currentRequest = request;
      loadVariantMappings();
      if(ms_variantMappings == null)
         return rows; // No variant mappings defined, nothing to do

      while (it.hasNext())
      {
         IPSSearchResultRow row = (IPSSearchResultRow) it.next();
         if(row.getColumnValue(m_thumbnail) == null)
            break; // If No thumbnail column then break out
                      // as there is nothing to do
         contentTypeId = row.getColumnValue(CONTENT_TYPE_ID);
         contentId = row.getColumnValue(CONTENT_ID);
         revision = row.getColumnValue(REVISION);
         variant = ms_variantMappings.getProperty(contentTypeId);
         if(variant == null)
            continue; // Could not find a mapped variant for this
                      // content type, so go to the next row.
         String assemblyUrl = getVariantAssemblyURL(contentTypeId, variant);
         if(assemblyUrl == null)
            continue;
         String url =
            createUrl(assemblyUrl, contentId, contentTypeId, variant, revision);
         row.setColumnValue(m_thumbnail, url);

      }
      return rows;
   }

   /**
    * Returns the assembly url which corresponds to the specified
    * contenttype and variant.
    * @param contenttype the content type id, assumed not <code>null</code>
    * @param variant the default thumbnail variant, assumed not
    * <code>null</code>
    * @return the assembly url or <code>null</code> if it could not be found.
    * @throws PSExtensionProcessingException upon any error
    */
   private String getVariantAssemblyURL(String contenttype, String variant)
      throws PSExtensionProcessingException
   {
      if(!m_variantUrls.containsKey(contenttype))
         loadVariantsByContentType(contenttype);
      Map variantUrls = (Map)m_variantUrls.get(contenttype);
      return variantUrls == null ? null : (String)variantUrls.get(variant);
   }

   /**
    * Loads contenttype to default thumbnail variant mappings
    * from sys_AddThumbnailURL.properties located in the server's
    * rxconfig/Server directory and caches them in a static context.
    *
    * @throws IOException upon an error when attempting to fetch the properties
    * file
    */
   private void loadVariantMappings()
      throws PSExtensionProcessingException
   {
      if(ms_variantMappings != null)
         return;
      File path = new File(PSServer.SERVER_DIR + "/" +VARIANT_MAP_FILE);
      if(!path.exists())
         return;
      ms_variantMappings = new Properties();
      InputStream in = null;
      try
      {
         in = new FileInputStream(path);
         ms_variantMappings.load(in);
      }
      catch(IOException ioe)
      {
         throw new PSExtensionProcessingException(
            "Error fetching variant mapping properties", ioe);
      }
      finally
      {
         try
         {
            if(in != null)
               in.close();
         }
         catch(IOException ignore){}
      }

   }

   /**
    * Lazy Loads the variants for the specified contenttype from the lookup
    * resource into the variant urls cache (local memory). We don't cache this in a static context
    * as new contenttypes and variants may be added and we don't want to
    * force a server restart to see them.
    * @param contenttype the contenttype id assumed not <code>null</code>.
    * @throws PSExtensionProcessingException upon internal request error.
    */
   private void loadVariantsByContentType(String contenttype)
      throws PSExtensionProcessingException
   {
      String req = VARIANT_LOOKUP_APP + "?contenttypeid=" + contenttype;
      IPSInternalRequest iReq = m_currentRequest.getInternalRequest(req);
      try
      {
         Document doc = iReq.getResultDoc();
         NodeList items = doc.getElementsByTagName("item");
         Map variantMap = new HashMap();
         int len = items.getLength();
         for(int i = 0; i < len; i++)
         {
            Element item = (Element)items.item(i);
            String variant = getElementValue(item, "value");
            String url = getElementValue(item, "assemblyurl");
            if(variant != null && url != null)
               variantMap.put(variant, url);
         }
         m_variantUrls.put(contenttype, variantMap);
      }
      catch (PSInternalRequestCallException e)
      {
         // Fatal error
         String[] args = {req, ""};
         throw new PSExtensionProcessingException(
               IPSCmsErrors.CMS_INTERNAL_REQUEST_ERROR, args);
      }
   }

   /**
    * Helper function that gets the value of the specified element under
    * the passed in parent element.
    * @param parent the parent element, assumed not <code>null</code>.
    * @param name the name of the element that the value will be extracted
    * from.
    * @return the value or <code>null</code> if no value found.
    */
   private String getElementValue(Element parent, String name)
   {
      NodeList nl = parent.getElementsByTagName(name);
      if(nl.getLength() == 0)
         return null;
      Node node = nl.item(0);
      if(!node.hasChildNodes())
         return null;
      NodeList children = node.getChildNodes();
      int len = children.getLength();
      for(int i = 0; i < len; i++)
      {
         Node child = children.item(i);
         if(child.getNodeType() == Node.TEXT_NODE)
            return ((Text)child).getData();
      }
      return null;

   }

   /**
    * Helper method to assemble the thumbnail preview url
    * @param assemblyUrl the assembly url for the specified variant, assumed
    * not <code>null</code>
    * @param contentid the content id of the current row, assumed not
    * <code>null</code>.
    * @param contenttype the content type id of the current row, assumed not
    * <code>null</code>.
    * @param variant the variant id of the current row, assumed not
    * <code>null</code>.
    * @param revision the revision of the current row, assumed not
    * <code>null</code>.
    * @return the url preview url, never <code>null</code>
    */
   private String createUrl(
      String assemblyUrl,
      String contentid,
      String contenttype,
      String variant,
      String revision)
   {
      StringBuffer sb = new StringBuffer();
      Map paramMap = new HashMap();
      paramMap.put(CONTENT_ID, contentid);
      paramMap.put(CONTENT_TYPE_ID, contenttype);
      paramMap.put(VARIANT, variant);
      paramMap.put(REVISION, revision);
      paramMap.put(AUTHTYPE, "0");
      paramMap.put(CONTEXT, "0");

      sb.append(assemblyUrl);
      sb.append("?");

      Iterator it = paramMap.keySet().iterator();
      boolean needsAmp = false;
      while(it.hasNext())
      {
         if(needsAmp)
            sb.append("&");
         else
            needsAmp = true;
         String key = (String)it.next();
         sb.append(key);
         sb.append("=");
         sb.append((String)paramMap.get(key));
      }

      return sb.toString();

   }

   /**
    * The cache of variant urls, loaded by
    * {@link #loadVariantsByContentType(String)}, never <code>null</code>.
    */
   private Map m_variantUrls = new HashMap();

   /**
    * The current request context as set by
    * {@link #processRows(Object[], List, IPSRequestContext).
    */
   private IPSRequestContext m_currentRequest;

   /**
    * A static cache variable to hold the variant mappings
    */
   private static Properties ms_variantMappings;

   /**
    * The name of the thumbnail column
    */
   private String m_thumbnail;

   /* Constants */
   private static final String AUTHTYPE = "sys_authtype";
   private static final String CONTENT_TYPE_ID = "sys_contenttypeid";
   private static final String CONTENT_ID = "sys_contentid";
   private static final String CONTEXT = "sys_context";
   private static final String REVISION = "sys_revision";
   private static final String THUMBNAIL = "sys_thumbnail";
   private static final String VARIANT = "sys_variantid";
   private static final String VARIANT_LOOKUP_APP = "sys_ceSupport/Variantlookup.xml";
   private static final String VARIANT_MAP_FILE = "addThumbnailURL.properties";

}
