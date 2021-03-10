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
package com.percussion.cms.objectstore.client;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSCataloger;
import com.percussion.cms.objectstore.PSRelationshipInfoSet;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.error.PSException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Cataloger used on the client side by the clients which cannot use designer
 * connection e.g cataloging client calls from the applet.
 */
public class PSRemoteCataloger implements IPSCataloger
{
	private Log logger = LogFactory.getLog(PSRemoteCataloger.class);
   /**
    * Ctor.
    * @param requester never <code>null</code>.
    */
   public PSRemoteCataloger(IPSRemoteRequester requester)
   {
      if (requester==null)
         throw new IllegalArgumentException("requester may not be null");

      m_requester = requester;
   }

   // see interface
   public Element getCEFieldXml(int controlFlags) throws PSCmsException
   {
      return getCEFieldXml(controlFlags, null);
   }
   
   //see the interface.
   @SuppressWarnings("unused")
   public Element getCEFieldXml(int controlFlags, Set<String> fields)
      throws PSCmsException
   {
      final Map<String, Object> params = new HashMap<>();
      if ((controlFlags & FLAG_INCLUDE_HIDDEN) > 0)
         params.put(IPSHtmlParameters.SYS_INCLUDEHIDDENFIELDS, "");
      if ((controlFlags & FLAG_INCLUDE_RESULTONLY) > 0)
         params.put(IPSHtmlParameters.SYS_INCLUDERESULTONLYFIELDS, "");
      if ((controlFlags & FLAG_RESTRICT_TOUSERCOMMUNITY) > 0)
         params.put(IPSHtmlParameters.SYS_RESTRICTFIELDSTOUSERCOMMUNITY, "");
      if ((controlFlags & FLAG_USER_SEARCH) > 0)
         params.put(IPSHtmlParameters.SYS_USERSEARCH, "");
      if ((controlFlags & FLAG_CTYPE_EXCLUDE_HIDDENFROMMENU) > 0)
         params.put(IPSHtmlParameters.SYS_CTYPESHIDEFROMMENU, "");
      if ((controlFlags & FLAG_EXCLUDE_CHOICES) > 0)
         params.put(IPSHtmlParameters.SYS_EXCLUDE_CHOICES, "");
      
      if (fields != null && !fields.isEmpty())
      {
         params.put(IPSHtmlParameters.SYS_CE_FIELD_NAME, 
            new ArrayList<String>(fields));
      }
    
      try{
    	  Document doc = getCatalogDocument(GET_CE_FIELDS, params);
    	  if(doc != null)
    		  return doc.getDocumentElement();   	   
      }catch(Throwable t){
    	  logger.error("An unexpected exception occurred while cataloging fields", t);
    	  throw new PSCmsException(IPSCmsErrors.CONTENT_TYPE_CANNOT_BE_OPENED);
      }
    return null;
   }

   // see interface
   public PSSearchConfig getSearchConfig() throws PSCmsException
   {
      Document doc = getCatalogDocument(GET_SEARCH_CONFIG, null);
      Element resultEl = doc.getDocumentElement();
      
      try
      {
         return new PSSearchConfig(resultEl, null, null);
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * See {@link IPSCataloger#getRelationshipInfoSet() interface}
    */
   public PSRelationshipInfoSet getRelationshipInfoSet()
      throws PSCmsException
   {
      Document doc = getCatalogDocument(GET_RELATE_INFO_SET, null);
      Element resultEl = doc.getDocumentElement();
      String expectedNodeName = PSRelationshipInfoSet.XML_NODE_NAME;

      if (! resultEl.getNodeName().equalsIgnoreCase(expectedNodeName))
      {
         String unknownDoc = PSXmlDocumentBuilder.toString(resultEl);
         String[] args = {GET_RELATE_INFO_SET, unknownDoc};
         throw new PSCmsException(IPSCmsErrors.UNEXPECTED_CATALOG_ERROR, args);
      }

      try
      {
         return new PSRelationshipInfoSet(resultEl);
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * Returns a reference to the IPSRemoteRequester.
    * @return reference to IPSRemoteRequester, never <code>null</code>.
    */
   public IPSRemoteRequester getRemoteRequester()
   {
      return m_requester;
   }

   /**
    * Get the catalogged document from the specified path/
    *
    * @param path The path that is used to get the catalogged document, assume
    *    not <code>null</code>.
    *
    * @param params Any html parameters that need to be included with the
    *    request. May be <code>null</code> or empty.
    *
    * @return The document that is received from the server, never
    *    <code>null</code>.
    *
    * @throws PSCmsException if an error occurs.
    */
   private Document getCatalogDocument(String path, Map params)
      throws PSCmsException
   {
      Document doc = null;
      try
      {
         if (null == params)
            params = new HashMap();
         doc = m_requester.getDocument(path, params);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         String[] args = {path, e.toString()};
         throw new PSCmsException(IPSCmsErrors.UNEXPECTED_CATALOG_ERROR, args);
      }

      return doc;
   }

   /**
    * The object which knows to communicate with the remote server. Initialized
    * by the ctor, never <code>null</code> after that.
    */
   private IPSRemoteRequester m_requester = null;

   private static final String CATALOGER_HANDLER = "sys_ceFieldsCataloger/";

   private static final String GET_CE_FIELDS =
      CATALOGER_HANDLER + "ContentEditorFields";

   private static final String GET_RELATE_INFO_SET =
      CATALOGER_HANDLER + "RelationshipInfoSet";
   
   private static final String GET_SEARCH_CONFIG =
      CATALOGER_HANDLER + "SearchConfig";
   
}
