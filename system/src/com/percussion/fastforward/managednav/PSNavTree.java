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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The root element of the navon tree. This class holds root specific
 * information.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavTree
{
   /**
    * Construct the entire tree.
    * 
    * @param req the parent request context.
    * @throws PSNavException
    */
   public PSNavTree(IPSRequestContext req) throws PSNavException
   {
      ms_config = PSNavConfig.getInstance(req);

      m_rootParams = PSNavUtil.buildStandardParams(req);
      m_theme = ms_config.getPropertyString(PSNavConfig.NAVTREE_THEME_DEFAULT);

      log.debug("Creating NavTree");
      PSNavUtil.logMap(m_rootParams, "HTML Parameters", log);

      PSLocator selfLoc = new PSLocator(req
            .getParameter(IPSHtmlParameters.SYS_CONTENTID));
      PSComponentSummary self = PSNavUtil.getItemSummary(req, selfLoc);
      log.debug("Self node is {}", self.getName());
      if (self.getContentTypeId() == ms_config.getNavTreeType())
      { // this is the root
         log.debug("starting at root");
         m_root = new PSNavon(req, self);
         m_root.setType(PSNavonType.TYPE_SELF);
         m_root.setRelativeLevel(0);
         loadNavTreeData(req, self.getCurrentLocator());
      }
      else if (self.getContentTypeId() == ms_config.getNavonType())
      { // this is a navon somewhere in the middle of the tree
         log.debug("starting in the middle");
         PSNavon selfNavon = new PSNavon(req, self);
         selfNavon.setType(PSNavonType.TYPE_SELF);
         selfNavon.setRelativeLevel(0);
         PSNavon top = selfNavon;
         while (top != null)
         {
            log.debug("walking up chain");
            PSNavon next = top.findParent(req);
            if (next == null)
            { // unexpected Navon with no parent.
               log.debug("found navon with no parent");
               this.m_root = top;
               break;
            }
            if (next.getType().getType() == PSNavonType.TYPE_ROOT)
            { // we found a NavTree content item
               this.m_root = next;
               loadNavTreeData(req, next.getLocator());
               break;
            }
            else
            {
               top = next;
            }
         }
      }
      m_root.setAbsoluteLevel(0);
      m_root.findChildren(req);

   }

   /**
    * Loads tree data from the SQL database
    * 
    * @param req
    * @param loc
    * @throws PSNavException
    */
   private void loadNavTreeData(IPSRequestContext req, PSLocator loc)
         throws PSNavException
   {
      try
      {

         PSContentTypeVariant whiteVar = ms_config.getNavtreeInfoVariant();
         Document tdoc = PSNavUtil.getVariantDocument(req, whiteVar, loc);

         this.m_theme = PSNavUtil.getFieldValueFromXML(tdoc, ms_config
               .getPropertyString(PSNavConfig.NAVTREE_THEME_FIELD));

         this.m_variableSelector = PSNavUtil.getFieldValueFromXML(tdoc, ms_config
               .getPropertyString(PSNavConfig.NAVON_VARIABLE_FIELD));

         this.m_imageSelector = PSNavUtil.getFieldValueFromXML(tdoc, ms_config
               .getPropertyString(PSNavConfig.NAVON_SELECTOR_FIELD));

      }
      catch (Exception e)
      {
         throw new PSNavException(e);
      }

   }

   /**
    * Serializes the tree to XML
    * 
    * @param req the parent request context.
    * @return
    */
   public Document toXml(IPSRequestContext req)
   {
      Document navtreeDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element rootElement = PSXmlDocumentBuilder.createRoot(navtreeDoc,
            XML_NODE_NAME);

      Iterator attrIterator = this.m_rootParams.keySet().iterator();
      while (attrIterator.hasNext())
      {
         String paramName = (String) attrIterator.next();
         String paramValue = this.m_rootParams.get(paramName).toString();
         rootElement.setAttribute(paramName, paramValue);
      }

      rootElement.setAttribute(XML_ATTR_THEME, this.m_theme);

      rootElement.setAttribute(XML_ATTR_SESSION, req.getUserSessionId());

      if (this.m_imageSelector != null && this.m_imageSelector.trim().length() > 0)
      {
         rootElement.setAttribute(XML_ATTR_SELECTOR, m_imageSelector);
      }

      if (this.m_variableSelector != null
            && this.m_variableSelector.trim().length() > 0)
      {
         rootElement.setAttribute(XML_ATTR_VARIABLE, m_variableSelector);
      }
      rootElement.appendChild(this.m_root.toXML(navtreeDoc));

      //Add shared/displaytitle element so that the variants can use the outer 
      //templates
      Element shared = PSXmlDocumentBuilder.addElement(navtreeDoc, rootElement,
            "shared", null);
      Element displaytitle = PSXmlDocumentBuilder.addElement(navtreeDoc,
            rootElement, "displaytitle", m_root.getName());
      shared.appendChild(displaytitle);
      rootElement.appendChild(shared);

      return navtreeDoc;
   }

   /**
    * Configuration instance.
    */
   private static PSNavConfig ms_config;

   /**
    * Writes the log.
    */
   private Logger log = LogManager.getLogger(this.getClass().getName());

   /**
    * Theme for the tree. The theme is used for selecting stylesheets in the
    * Navon variants.
    */
   private String m_theme;

   /**
    * The context variable selector value for the tree.
    */
   private String m_variableSelector;

   /**
    * The image color selector for the tree.
    */
   private String m_imageSelector;

   /**
    * The root Navon.
    */
   private PSNavon m_root;

   /**
    * The parameters for the root XML node.
    */
   private Map m_rootParams;

   /**
    * XML Element name for serialization.
    */
   public static final String XML_NODE_NAME = "navtree";

   /**
    * XML Attribute for the theme.
    */
   public static final String XML_ATTR_THEME = "theme";

   /**
    * XML Attribute for the image color selector
    */
   public static final String XML_ATTR_SELECTOR = "selector";

   /**
    * XML Attribute for the variable selector.
    */
   public static final String XML_ATTR_VARIABLE = "varname";

   /**
    * XML Attribute for the session id.
    */
   public static final String XML_ATTR_SESSION = "pssessionid";

   /**
    * HTML parameter for theme override. The default theme will be used if this
    * value is not specified.
    */
   public static final String HTML_PARAM_THEME = "nav_theme";

}
