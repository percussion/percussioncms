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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Catalogs all server security providers by querying the
 * ../sys_components/getSecurityProviders.xml app.
 */
public class PSSecurityProviderCataloger
{
   /**
    * Constructor meant to be used in the context of an applet. This may not work
    * in other contexts since there is no way of supplying credentials for logging
    * in.
    * @param urlBase the document or code base for the applet.
    * @throws PSCmsException if request to server to get the data fails for
    * any reason.
    */
   public PSSecurityProviderCataloger(URL urlBase)
      throws PSCmsException
   {
      try
      {
         URL url = new URL(urlBase, RESOURCE);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            url.openStream(), false);
         fromXml(doc.getDocumentElement());
      }
      catch(Exception e)
      {
         throw new PSCmsException(1000, e.getMessage());
      }
   }

   /**
    * Extracts <code>PSSecurityProviderInstanceSummary</code> Nodes
    * from the xml, creates the instances and adds them to
    * the provider list.
    *
    * @param src the element representing the list of provider summaries.
    * May be <code>null</code>.
    *
    * Expects Xml document based on the following DTD:
    *
    * <code><pre>
    *
    *  &lt;!ELEMENT SecurityProviders (PSXSecurityProviderInstanceSummary*)&gt;
    *  &lt;!ELEMENT PSXSecurityProviderInstanceSummary (name)&gt;
    *   &lt;!ATTLIST
    *    typeName  CDATA       #REQUIRED
    *    typeId     CDATA       #REQUIRED
    *   &gt;
    *
    *  &lt;!ELEMENT name       (#PCDATA)&gt;
    *
    * </pre></code>
    */
   public void fromXml(Element src) throws PSUnknownNodeTypeException
   {
      if(null == src)
         return;

      Element elem = null;
      NodeList nodes =
         src.getElementsByTagName(PSSecurityProviderInstanceSummary.XML_ELEMENT_ROOT);
      for(int i = 0; i < nodes.getLength(); i++)
      {
         elem = (Element)nodes.item(i);
         m_providers.add(new PSSecurityProviderInstanceSummary(elem));
      }

   }


   /**
    * Return iterator of providers.
    * @return iterator. Never <code>null</code>.
    */
   public Iterator getProviders()
   {
      return m_providers.iterator();
   }

   /**
    * Return provider by id
    * @param id id of provider to be found
    * @return provider if found, else <code>null</code>.
    */
   public PSSecurityProviderInstanceSummary getProviderById(int id)
   {
      PSSecurityProviderInstanceSummary provider = null;
      Iterator it = getProviders();
      while(it.hasNext())
      {
         provider = (PSSecurityProviderInstanceSummary)it.next();
         if(provider.getTypeId() == id)
            return provider;
      }
      return null;
   }

   /**
    * Return provider by name.
    * @param name name of provider to be found
    * @return provider if found, else <code>null</code>.
    */
   public PSSecurityProviderInstanceSummary getProviderByName(String name)
   {
      PSSecurityProviderInstanceSummary provider = null;
      Iterator it = getProviders();
      while(it.hasNext())
      {
         provider = (PSSecurityProviderInstanceSummary)it.next();
         if(provider.getTypeName().equals(name))
            return provider;
      }
      return null;
   }


   /**
    * This is a list of lists of strings. The parent list contains provider type
    * names, the sublist contains a lists of provider type instance names.
    */
   private List m_providers = new ArrayList();

   /**
    * The security providers XML root element node.
    */
   static public final String XML_ELEM_ROOT = "SecurityProviders";


   /**
    * The application resource that fetches the security providers.
    */
   private static final String RESOURCE =
      "../sys_components/getSecurityProviders.xml";
}
