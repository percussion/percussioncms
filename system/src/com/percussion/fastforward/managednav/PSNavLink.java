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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A link to a landing page or image. The
 * 
 * @author DavidBenua
 *  
 */
public class PSNavLink
{
   /**
    * Default constructor
    */
   public PSNavLink()
   {

   }

   /**
    * Gets the content id.
    * 
    * @return contentId
    */
   public int getContentId()
   {
      return m_contentId;
   }

   /**
    * Gets the folder id.
    * 
    * @return folderId
    */
   public int getFolderId()
   {
      return m_folderId;
   }

   /**
    * Gets the URI.
    * 
    * @return the URI
    */
   public String getURI()
   {
      return m_uri;
   }

   /**
    * Gets the variantid.
    * 
    * @return the variant id.
    */
   public int getVariantId()
   {
      return m_variantId;
   }

   /**
    * Sets the conentid.
    * 
    * @param contentId the new content id.
    */
   public void setContentId(int contentId)
   {
      m_contentId = contentId;
   }

   /**
    * Sets the folderid.
    * 
    * @param folderId the new folder id.
    */
   public void setFolderId(int folderId)
   {
      m_folderId = folderId;
   }

   /**
    * Sets the URI.
    * 
    * @param URI the new URI.
    */
   public void setURI(String URI)
   {
      m_uri = URI;
   }

   /**
    * Sets the variant id.
    * 
    * @param variantId the new variant id.
    */
   public void setVariantId(int variantId)
   {
      m_variantId = variantId;
   }

   /**
    * builds a link to a specific variant of the specified content item.
    * <p>
    * Use this method when you want to link to a specific variant, independent
    * of what is in a slot.
    * <p>
    * <p>
    * This method always creates an internal link, suitable for use in XSL
    * stylesheets
    * </p>
    * 
    * @param req the parent request context
    * @param summary points to the document which is linked
    * @param variantId the variant id to link to.
    * @throws PSNavException if any unexpected exceptions happen.
    */
   public void createLinkToDocument(IPSRequestContext req,
         PSNavComponentSummary summary, int variantId) throws PSNavException
   {
      PSContentTypeVariant variant = PSNavUtil.loadVariantInfo(req, summary
            .getContentTypeId(), variantId);
      if (variant == null)
      {
         log.error(MSG_UNABLE_TO_FIND_LINK);
         throw new PSNavException(MSG_UNABLE_TO_FIND_LINK);
      }
      createLinkToDocument(req, summary, variant);
   }

   /**
    * Convenience method for createLineToDocument(IPSRequestContext,
    * PSComponentSummary, int, Map).
    * 
    * @param req the parent request context
    * @param summary the document to link to.
    * @param variant the variant id for the link.
    * @throws PSNavException
    */
   public void createLinkToDocument(IPSRequestContext req,
         PSNavComponentSummary summary, PSContentTypeVariant variant)
         throws PSNavException
   {
      createLinkToDocument(req, summary, variant, Collections.EMPTY_MAP);
   }

   /**
    * builds a link to a specific variant of the specified content item.
    * <p>
    * Use this method when you want to link to a specific variant, independent
    * of what is in a slot.
    * <p>
    * <p>
    * This method always creates an internal link, suitable for use in XSL
    * stylesheets
    * </p>
    * 
    * @param req the parent request context
    * @param summary points to the document which is linked
    * @param variant the variant to link to.
    * @param extraParams
    * @throws PSNavException if any unexpected exceptions happen.
    */

   public void createLinkToDocument(IPSRequestContext req,
         PSComponentSummary summary, PSContentTypeVariant variant,
         Map extraParams) throws PSNavException
   {
      PSNavComponentSummary navsum = new PSNavComponentSummary(summary);
      createLinkToDocument(req, navsum, variant, extraParams);
   }

   /**
    * Create a server local assembly navlink URL for the specified item, variant
    * and extra parameter map.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param summary component summary object for the nav item, must not be
    *           <code>null</code>.
    * @param variant conten type vriant object to build the link url for, must
    *           nto be <code>null</code>.
    * @param extraParams map if extra parameters to add to the url, must not be
    *           <code>null</code>, may be empty.
    * @throws PSNavException if a link could not be generated.
    */
   public void createLinkToDocument(IPSRequestContext req,
         PSNavComponentSummary summary, PSContentTypeVariant variant,
         Map extraParams) throws PSNavException
   {
      m_variantId = variant.getVariantId();
      m_contentId = summary.getCurrentLocator().getId();
      m_revision = summary.getCurrentLocator().getRevision();
      String folderid = (String) extraParams
            .get(IPSHtmlParameters.SYS_FOLDERID);
      if (folderid != null && folderid.trim().length() > 0)
         m_folderId = Integer.parseInt(folderid.trim());
      String sessionId = req.getUserSessionId();

      try
      {
         Map params = buildLinkParams(summary, variant, req);
         params.putAll(extraParams);

         params.put(IPSHtmlParameters.SYS_SESSIONID, sessionId);

         URL intURL = PSUrlUtils.createUrl("127.0.0.1", new Integer(req
               .getOriginalPort()), variant.getAssemblyUrl(), params.entrySet()
               .iterator(), (String) null, //no Anchor
               req);

         m_uri = intURL.toString();
      }
      catch (MalformedURLException e)
      {
         log.error("Malformed URL {} ", e.getMessage());
         throw new PSNavException(e);
      }

   }

   /**
    * Convenience method for createLinkFromSnippet(IPSRequestContext,
    * PSNavComponentSummary, PSContentTypeVariant). Creates a link by reading a
    * snippet.
    * 
    * @param req the parent request context.
    * @param summary the content item to link to.
    * @param variantId the variant to link to.
    * @throws PSNavException
    */
   public void createLinkFromSnippet(IPSRequestContext req,
         PSNavComponentSummary summary, int variantId) throws PSNavException
   {
      PSContentTypeVariant variant = PSNavUtil.loadVariantInfo(req, summary
            .getContentTypeId(), variantId);
      if (variant == null)
      {
         log.error(MSG_UNABLE_TO_FIND_LINK);
         throw new PSNavException(MSG_UNABLE_TO_FIND_LINK);
      }
      createLinkFromSnippet(req, summary, variant,null);
   }

   /**
    * fills in the current link object by building a snippet and taking the href
    * attribute of the first link in the snippet as the URL.
    * 
    * Use this method when you want to follow the snippet that is in a slot to
    * build a link to its page item.
    * 
    * @param req the parent request
    * @param summary Component summary of the target document to link.
    * @param variant the variant object for the snippet variant.
    * @throws PSNavException when any exception is encountered.
    */
   public void createLinkFromSnippet(IPSRequestContext req,
         PSNavComponentSummary summary, PSContentTypeVariant variant)
         throws PSNavException
   {
      createLinkFromSnippet(req, summary, variant, null);
   }
   /**
    * fills in the current link object by building a snippet and taking the href
    * attribute of the first link in the snippet as the URL.
    * 
    * Use this method when you want to follow the snippet that is in a slot to
    * build a link to its page item.
    * 
    * @param req the parent request
    * @param summary Component summary of the target document to link.
    * @param variant the variant object for the snippet variant.
    * @param extraParams that needs to be passed to the link. If 
    * <code>null</code> or <code>empty</code> no params will be added.
    * @throws PSNavException when any exception is encountered.
    */
   public void createLinkFromSnippet(IPSRequestContext req,
         PSNavComponentSummary summary, PSContentTypeVariant variant,
         Map extraParams) throws PSNavException
   {
      
      m_contentId = summary.getCurrentLocator().getId();
      m_revision = summary.getCurrentLocator().getRevision();
      m_variantId = variant.getVariantId();
      String folderid = (String) extraParams
            .get(IPSHtmlParameters.SYS_FOLDERID);
      if (folderid != null && folderid.trim().length() > 0)
         m_folderId = Integer.parseInt(folderid);

      log.debug("creating link to snippet {}", variant.getName());

      try
      {

         Map params = buildLinkParams(summary, variant, req);
         if(extraParams != null && extraParams.size()>0)
            params.putAll(extraParams);
         if (log.isDebugEnabled())
         {
            PSNavUtil.logMap(params, "internal link params", log);
         }

         IPSInternalRequest ir = req.getInternalRequest(variant
               .getAssemblyUrl(), params, false);
         if (ir == null)
         {
            throw new PSNavException(MSG_VARIANT_ASSEMBLER);
         }
         byte[] snippetData = ir.getMergedResult();

         log.debug("snippet data - {} ", String.valueOf(snippetData));

         Document doc = PSXmlDocumentBuilder.createXmlDocument(
               new ByteArrayInputStream(snippetData), false);
         log.debug("loaded document");
         NodeList links = doc.getElementsByTagName("a");
         if (links.getLength() > 0)
         {
            Element firstElem = (Element) links.item(0);
            m_uri = firstElem.getAttribute("href");
            //URI = PSXmlTreeWalker.getElementData(firstElem);
            if (m_uri == null)
            {
               log.warn("snippet without @href attribute found  \n {} ", PSXmlDocumentBuilder.toString(doc));
            }
            log.debug("Link URI is {} ", m_uri);
         }

      }
      catch (Exception ex)
      {
         throw new PSNavException(ex);
      }
   }


   /**
    * Builds a link from a relationship. The dependent object is set as the
    * link.
    * 
    * @param req the parent request context.
    * @param relation the relationhip to link to
    * @param useVariant the variant to use. If this parameter is
    *           <code>null</code> the variant from the relationship is used.
    *           This variant must be a page variant if <code>followLink</code>
    *           is <code>false</code>.
    * @param followLink determines if this link contains the URL of the linked
    *           item or the URL of the first <code><a></code> tag. If this
    *           value is true, the link snippet will be assembled.
    * @throws PSNavException
    */
   public void buildLinkFromRelationship(IPSRequestContext req,
         PSAaRelationship relation, PSContentTypeVariant useVariant,
         boolean followLink) throws PSNavException
   {
      log.debug("building link from relationship");

      PSLocator childLoc = relation.getDependent();
      Map extraParams = new HashMap();
      String folderid = relation.getProperty(IPSHtmlParameters.SYS_FOLDERID);
      if (folderid != null && folderid.trim().length() > 0)
      {
         extraParams.put(IPSHtmlParameters.SYS_FOLDERID, folderid.trim());
      }
      log.debug("loading child summary");
      PSNavComponentSummary childSummary = new PSNavComponentSummary(childLoc);
      //PSNavUtil.getItemSummary(req, childLoc);
      if (childSummary == null)
      {
         log.error("Dependent item not found");
         throw new PSNavException("Dependent item not found");
      }

      PSContentTypeVariant ourVariant = null;
      if (useVariant != null)
      {
         ourVariant = useVariant;
      }
      else
      {
         ourVariant = relation.getVariant();
      }
      if (followLink)
      {
         log.debug("following link");
         createLinkFromSnippet(req, childSummary, ourVariant,extraParams);
      }
      else
      {
         log.debug("creating link to document");
         createLinkToDocument(req, childSummary, ourVariant,extraParams);
      }

   }

   /**
    * Convenience method for buildLinkParams(PSNavComponentSummary,
    * PSContentTypeVariant, IPSRequestContext. Builds the mape of parameters for
    * a link, but uses PSNavComponentSummary instead.
    * 
    * @param summary the content item to link to
    * @param variant the variant to use.
    * @param req the parent request context.
    * @return the paraemters for
    */
   private Map buildLinkParams(PSComponentSummary summary,
         PSContentTypeVariant variant, IPSRequestContext req)
   {
      PSNavComponentSummary navsum = new PSNavComponentSummary(summary);
      return buildLinkParams(navsum, variant, req);
   }

   /**
    * Builds the map of standard parmaeters for a link.
    * 
    * @param summary the item summary
    * @param variant the variant to link to.
    * @param req the parent request context.
    * @return the map of params. Never <code>null</code> but may be
    *         <code>Empty</code>.
    */
   private Map buildLinkParams(PSNavComponentSummary summary,
         PSContentTypeVariant variant, IPSRequestContext req)
   {
      PSLocator currentLoc = summary.getCurrentLocator();
      String contentId = String.valueOf(currentLoc.getId());
      String revision = String.valueOf(currentLoc.getRevision());
      String variantId = String.valueOf(variant.getVariantId());

      Map params = new HashMap();
      params.put(IPSHtmlParameters.SYS_CONTENTID, contentId);
      params.put(IPSHtmlParameters.SYS_REVISION, revision);
      params.put(IPSHtmlParameters.SYS_VARIANTID, variantId);

      String context = req.getParameter(IPSHtmlParameters.SYS_CONTEXT);
      if (context != null)
      {
         params.put(IPSHtmlParameters.SYS_CONTEXT, context);
      }

      String authType = req.getParameter(IPSHtmlParameters.SYS_AUTHTYPE);
      if (authType != null)
      {
         params.put(IPSHtmlParameters.SYS_AUTHTYPE, authType);
      }

      String siteId = req.getParameter(IPSHtmlParameters.SYS_SITEID);
      if (siteId != null)
      {
         params.put(IPSHtmlParameters.SYS_SITEID, siteId);
      }
      String command = req.getParameter(IPSHtmlParameters.SYS_COMMAND);
      if (command != null && command.equalsIgnoreCase("editrc"))
      {
         params.put(IPSHtmlParameters.SYS_COMMAND, command);
      }
      return params;
   }

   /**
    * Builds the XML representation of a link. The new XML element is attached
    * to the specfied parent element.
    * 
    * @param parentElem the parent element
    * @return the parent element.
    */
   public Element toXML(Element parentElem)
   {
      parentElem.setAttribute(XML_ATTR_CONTENTID, String
            .valueOf(m_contentId));
      parentElem.setAttribute(XML_ATTR_VARIANTID, String
            .valueOf(m_variantId));
      parentElem.setAttribute(XML_ATTR_REVISION, String.valueOf(m_revision));
      if (m_folderId != 0)
         parentElem.setAttribute(XML_ATTR_FOLDERID, String.valueOf(m_folderId));
         
      Document parentDoc = parentElem.getOwnerDocument();
      Node textNode = parentDoc.createTextNode(m_uri);
      parentElem.appendChild(textNode);

      return parentElem;
   }

   /**
    * URI to link to
    */
   String m_uri;

   /**
    * Content Id of linked object
    */
   int m_contentId;

   /**
    * Folder Id of linked object this gets initialized through
    * {@link #createLinkFromSnippet(IPSRequestContext, 
    *    PSNavComponentSummary, PSContentTypeVariant, Map)};
    * or {@link #createLinkToDocument(IPSRequestContext, 
    * PSNavComponentSummary, PSContentTypeVariant, Map)};
    * methods, if the Map consists of IPSHtmlParameters.SYS_FOLDERID.
    * otherwise the value will be 0;
    *  
    */
   int m_folderId;

   /**
    * Variant of linked object
    */
   int m_variantId;

   /**
    * Revision of linked object
    */
   private int m_revision;

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private Logger log = LogManager.getLogger(getClass());

   /**
    * Error message for missing link
    */
   private static final String MSG_UNABLE_TO_FIND_LINK = "Unable to find variant for link";

   /**
    * Error message for missing assembler.
    */
   private static final String MSG_VARIANT_ASSEMBLER = "Variant assembly resource not found";

   /**
    * XML Attribute for content id.
    */
   private static final String XML_ATTR_CONTENTID = IPSHtmlParameters.SYS_CONTENTID;

   /**
    * XML Attribute for variant id.
    */
   private static final String XML_ATTR_VARIANTID = IPSHtmlParameters.SYS_VARIANTID;

   /**
    * XML Attribute for revision id.
    */
   private static final String XML_ATTR_REVISION = IPSHtmlParameters.SYS_REVISION;

   /**
    * XML Attribute for folder id.
    */
   private static final String XML_ATTR_FOLDERID = IPSHtmlParameters.SYS_FOLDERID;

}
