/******************************************************************************
 *
 * [ PSSiteCataloger.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx.catalogers;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSSite;
import com.percussion.cx.PSFolderActionManager;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * This class catalogs all site definitions found in rhythmyx.
 */
public class PSSiteCataloger
{
   /**
    * Constructs a new site definition cataloger.
    * 
    * @param urlBase the base URL to use for the catalog request, not
    *    <code>null</code>.
    * @throws PSCmsException for any error.
    */
   public PSSiteCataloger(URL urlBase) throws PSCmsException
   {
      if (urlBase == null)
         throw new IllegalArgumentException("urlBase cannot be null");
      m_urlBase = urlBase;
      init();
   }

   /**
    * Queries the Rx server to obtain the list of all sites.
    * @throws PSCmsException If the communication fails.
    */
   private void init()
      throws PSCmsException
   {
      try
      {
         URL url = new URL(m_urlBase, 
            "../sys_pubSites/getSites.xml");
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            url.openStream(), false);
         
         fromXml(doc.getDocumentElement());
      }
      catch (Exception e)
      {
         throw new PSCmsException(IPSContentExplorerErrors.CATALOG_ERROR, e
            .getMessage());
      }      
   }
   
   /**
    * Loads the site names from the supplied XML element. For the expected 
    * DTD see {@link PSSite#toXml(Document)}.
    * 
    * @param elemRoot the XML element from which to load the site names, 
    *    assumed not <code>null</code>.
    * @throws PSUnknownNodeTypeException for any unknown XML node.
    */
   private void fromXml(Element elemRoot) throws PSUnknownNodeTypeException
   {
      m_sites.clear();

      PSXMLDomUtil.checkNode(elemRoot, ROOT_ELEM);
      
      NodeList sites = elemRoot.getElementsByTagName(PSSite.XML_NODE_NAME);
      for (int i=0; i<sites.getLength(); i++)
      {
         Element site = (Element) sites.item(i);
            Attr isPageBased = site.getAttributeNode("isPageBased");
            if(isPageBased != null && "T".equals(isPageBased.getValue()) ){
               PSFolderActionManager.addCM1SiteRootFolder(site.getAttribute("folderRoot"));
            }else{
               m_sites.add(new PSSite(site, null, null));
            }
      }
   }

   /**
    * Attempts to get new data and if successful, replaces the existing data.
    * 
    * @throws PSCmsException If any problems communicating with the server.
    */
   public void refresh()
      throws PSCmsException
   {
      init();
   }
   
   /**
    * Get the collection of all defined site names.
    * 
    * @return a collection of all site definitions as <code>PSSite</code>
    *    objects, never <code>null</code>, may be empty.
    */
   public Collection getSites()
   {
      return Collections.unmodifiableCollection(m_sites);
   }

   /**
    * Used to make requests to the Rx server. Set in ctor, then never 
    * <code>null</code>, or modified.
    */
   private URL m_urlBase = null;
   
   /**
    * The collection of all sites, reset with each call to 
    * {@link #fromXml(Element)}, never <code>null</code>, may be empty.
    */
   private Collection m_sites = new ArrayList();
   
   // private XML constants
   private static final String ROOT_ELEM = "Sites";
}