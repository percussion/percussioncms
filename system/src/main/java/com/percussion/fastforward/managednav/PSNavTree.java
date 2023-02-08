/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.fastforward.managednav;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeTemplate;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.Map;

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
      m_theme = ms_config.getNavTreeThemeDefault();

      log.debug("Creating NavTree");
      PSNavUtil.logMap(m_rootParams, "HTML Parameters", log);

      PSLocator selfLoc = new PSLocator(req
            .getParameter(IPSHtmlParameters.SYS_CONTENTID));
      PSComponentSummary self = PSNavUtil.getItemSummary(req, selfLoc);
      log.debug("Self node is {}", self.getName());
      if (ms_config.getNavTreeTypeIds().contains(self.getContentTypeId()))
      { // this is the root
         log.debug("starting at root");
         m_root = new PSNavon(req, self);
         m_root.setType(PSNavonType.TYPE_SELF);
         m_root.setRelativeLevel(0);
         loadNavTreeData(req, self.getCurrentLocator());
      }
      else if (ms_config.getNavonTypeIds().contains(self.getContentTypeId()))
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

         PSContentTypeTemplate whiteVar = ms_config.getNavTreeInfoTemplate();
         Document tdoc = PSNavUtil.getVariantDocument(req, whiteVar, loc);

         this.m_theme = PSNavUtil.getFieldValueFromXML(tdoc,
                 ms_config.getNavThemeParamName());

         this.m_variableSelector = PSNavUtil.getFieldValueFromXML(tdoc,
                 ms_config.getNavonVariableName());

         this.m_imageSelector = PSNavUtil.getFieldValueFromXML(tdoc,
                 ms_config.getNavonSelectorField());

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

      for (Object o : this.m_rootParams.keySet()) {
         String paramName = (String) o;
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
   private static final Logger log = LogManager.getLogger(IPSConstants.NAVIGATION_LOG);

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
