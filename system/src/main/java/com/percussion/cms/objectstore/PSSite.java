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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.IPSComponent;
import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This object represents a site definition. See {@link #toXml(Document)} for
 * the expected DTD.
 */
public class PSSite extends PSComponent
{
   /**
    * Constructs the site from its XML representation.
    * 
    * @see IPSComponent#fromXml(Element, IPSDocument, List) for parameter
    *    descriptions.
    */
   public PSSite(Element source, IPSDocument parent,
      List parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(source, parent, parentComponents);
   }
   
   /* (non-Javadoc)
    * @see PSComponent#copyFrom(PSComponent) for documentation.
    */
   public void copyFrom(PSSite c)
   {
      super.copyFrom(c);
      
      m_name = c.m_name;
      m_description = c.m_description;
      m_baseUrl = c.m_baseUrl;
      m_root = c.m_root;
      m_ipAddress = c.m_ipAddress;
      m_port = c.m_port;
      m_userId = c.m_userId;
      m_password = c.m_password;
      m_state = c.m_state;
      m_folderRoot = c.m_folderRoot;
      m_navTheme = c.m_navTheme;
      m_globalTemplate = c.m_globalTemplate;
   }
   
   /* (non-Javadoc)
    * @see IPSComponent#clone() for documentation.
    */
   @Override
   public Object clone()
   {
      PSSite clone = (PSSite) super.clone();
      clone.copyFrom(this);
      
      return clone;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSSite)) return false;
      if (!super.equals(o)) return false;
      PSSite psSite = (PSSite) o;
      return isPageBased() == psSite.isPageBased() && m_name.equals(psSite.m_name) && Objects.equals(m_description, psSite.m_description) && Objects.equals(m_baseUrl, psSite.m_baseUrl) && Objects.equals(m_root, psSite.m_root) && Objects.equals(m_ipAddress, psSite.m_ipAddress) && Objects.equals(m_port, psSite.m_port) && Objects.equals(m_userId, psSite.m_userId) && Objects.equals(m_password, psSite.m_password) && Objects.equals(m_state, psSite.m_state) && Objects.equals(m_folderRoot, psSite.m_folderRoot) && Objects.equals(m_navTheme, psSite.m_navTheme) && Objects.equals(m_globalTemplate, psSite.m_globalTemplate);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_description, m_baseUrl, m_root, m_ipAddress, m_port, m_userId, m_password, m_state, m_folderRoot, m_navTheme, m_globalTemplate, isPageBased());
   }

   /**
    * Creates the XML presentation of this object conforming to the following
    * DTD:
    * &lt;!ELEMENT Sites (PSXSite*)&gt;
    * &lt;!ATTLIST Site
    *    id CDATA #IMPLIED
    *    name CDATA #REQUIRED
    *    description CDATA #IMPLIED
    *    baseUrl CDATA #IMPLIED
    *    homePageUrl CDATA #IMPLIED
    *    folderRoot CDATA #IMPLIED
    *    globalTemplate CDATA #IMPLIED
    *    ipAddress CDATA #IMPLIED
    *    navTheme CDATA #IMPLIED
    *    password CDATA #IMPLIED
    *    port CDATA #IMPLIED
    *    root CDATA #IMPLIED
    *    state CDATA #REQUIRED
    *    userId CDATA #IMPLIED
    * &gt;
    * 
    * @see IPSComponent#fromXml(Element, IPSDocument, List)
    */
   public void fromXml(Element source, IPSDocument parent,
      List parentComponents) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(source.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      super.fromXml(source);
      
      m_name = source.getAttribute(NAME_ATTR);
      if (m_name == null || m_name.trim().length() == 0)
      {
         Object[] args =
         {
            XML_NODE_NAME,
            NAME_ATTR,
            m_name
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
      m_name = m_name.trim();
      
      m_description = source.getAttribute(DESCRIPTION_ATTR);
      m_baseUrl = source.getAttribute(BASEURL_ATTR);
      m_root = source.getAttribute(ROOT_ATTR);

      m_ipAddress = source.getAttribute(IPADDRESS_ATTR);
      m_port = source.getAttribute(PORT_ATTR);
      m_userId = source.getAttribute(USERID_ATTR);
      m_password = source.getAttribute(PASSWORD_ATTR);
      m_state = source.getAttribute(STATE_ATTR);
      if (m_state == null || m_state.trim().length() == 0)
         m_state = String.valueOf(STATE_ACTIVE);

      //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16 
      m_state = m_state.trim();
      int test = -1;
      try
      {
         test = Integer.parseInt(m_state);
      }
      catch (NumberFormatException e)
      {
         // ignore error and set to default value
         m_state = String.valueOf(STATE_ACTIVE);
      }
      if (!isValid(test, VALID_STATES))
      {
         Object[] args =
         {
            XML_NODE_NAME,
            STATE_ATTR,
            m_state
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      m_folderRoot = source.getAttribute(FOLDERROOT_ATTR);
      m_navTheme = source.getAttribute(NAVTHEME_ATTR);
      m_globalTemplate = source.getAttribute(GLOBALTEMPLATE_ATTR);
      setPageBased(Boolean.parseBoolean(source.getAttribute(IS_PAGE_BASED_ATTR)));
   }

   /* (non-Javadoc)
    * @see IPSComponent#toXml(Document)
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      super.toXml(root);
      
      root.setAttribute(NAME_ATTR, m_name);
      if (m_description != null)
         root.setAttribute(DESCRIPTION_ATTR, m_description);
      if (m_baseUrl != null)
         root.setAttribute(BASEURL_ATTR, m_baseUrl);
      if (m_ipAddress != null)
         root.setAttribute(IPADDRESS_ATTR, m_ipAddress);
      if (m_userId != null)
         root.setAttribute(USERID_ATTR, m_userId);
      if (m_password != null)
         root.setAttribute(PASSWORD_ATTR, m_password);
      if (m_state != null)
         root.setAttribute(STATE_ATTR, m_state);
      if (m_folderRoot != null)
         root.setAttribute(FOLDERROOT_ATTR, m_folderRoot);
      if (m_navTheme != null)
         root.setAttribute(NAVTHEME_ATTR, m_navTheme);
      if (m_globalTemplate != null)
         root.setAttribute(GLOBALTEMPLATE_ATTR, m_globalTemplate);

       root.setAttribute(IS_PAGE_BASED_ATTR, Boolean.toString(isPageBased()));

      return root;
   }
   
   /**
    * Get the site name.
    * 
    * @return the site name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Set a new site name.
    * 
    * @param name the new site name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
      
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");

      m_name = name;
   }

   public boolean isPageBased() {
      return isPageBased;
   }

   public void setPageBased(boolean pageBased) {
      isPageBased = pageBased;
   }

   /**
    * Get the folder root of this site.
    * 
    * @return the folder root, may be <code>null</code> or empty.
    */
   public String getFolderRoot()
   {
      return m_folderRoot;
   }
   
   /**
    * Set the new folder root for this site.
    * 
    * @param folderRoot the new folder root, may be <code>null</code> or empty.
    */
   public void setFolderRoot(String folderRoot)
   {
      m_folderRoot = folderRoot;
   }
   
   /**
    * Get the sites global template.
    * 
    * @return the global template file name, may be <code>null</code>.
    */
   public String getGlobalTemplate()
   {
      return m_globalTemplate;
   }
   
   /**
    * Get the global template name.
    * 
    * @return the global template name, which is the global template file
    *    name without the file suffix '.xsl', may be <code>null</code>.
    */
   public String getGlobalTemplateName()
   {
      if (m_globalTemplate != null)
      {
         int pos = m_globalTemplate.indexOf(".xsl");
         
         return pos != -1 ? 
            m_globalTemplate.substring(0, pos) : m_globalTemplate;
      }
      
      return null;
   }
   
   /**
    * Get a list with all requested site definitions.
    * 
    * @param request the request to be used for the lookup, assumed not 
    *    <code>null</code>. The selection filters <code>sys_siteid</code>, 
    *    <code>sys_sitename</code> and <code>sys_folderroot</code> can be 
    *    set as HTML parameters. Not provided parameters will be omitted for
    *    the selection.
    * @return a list of <code>PSSite</code> objects with all requested site 
    *    definitions, never <code>null</code>, may be empty.
    * @throws PSCmsException for any error.
    */
   public static List getSites(IPSRequestContext request) throws PSCmsException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      // get all possible parameters from the request. This is to make sure
      // the resource cache is only based on these possible parameters, and
      // not based by any unknown parameters in the request.
      Map params = new HashMap();
      String paramValue;
      for (int i=0; i<ALL_PARAMS.length; i++)
      {
         paramValue = request.getParameter(ALL_PARAMS[i]);
         if (paramValue != null && paramValue.trim().length() > 0)
            params.put(ALL_PARAMS[i], paramValue);
      }
      
      try
      {
         List results = new ArrayList();
         
         IPSInternalRequest ir = request.getInternalRequest(
               GET_SITE_RESOURCE, params, false);
         Document doc = ir.getResultDoc();
         NodeList sites = doc.getElementsByTagName(PSSite.XML_NODE_NAME);
         for (int i=0; i<sites.getLength(); i++)
         {
            Element site = (Element) sites.item(i);
            results.add(new PSSite(site, null, null));
         }
         
         return results;
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }
   
   /**
    * Returns the site name, never <code>null</code> or empty.
    */
   @Override
   public String toString()
   {
      return getName();
   }
   
   /**
    * Tests if the supplied value is defined in the supplied array.
    * 
    * @param value the value to test.
    * @param validValues an array with all valid values, assumed not
    *    <code>null</code>.
    * @return <code>true</code> if the supplied value is defined in the
    *    provided values array, <code>false</code> otherwise.
    */
   private boolean isValid(int value, int[] validValues)
   {
      boolean isValid = false;
      for (int i=0; i<validValues.length && !isValid; i++)
         isValid = validValues[i] == value;
      
      return isValid;
   }
   
   /**
    * The site name, initialized during construction, never <code>null</code>
    * or empty after that.
    */
   private String m_name = null;
   
   /**
    * The site description, may be <code>null</code> or empty.
    */
   private String m_description = null;
   
   /**
    * The site base url, may be <code>null</code> or empty.
    */
   private String m_baseUrl = null;
   
   /**
    * The site root, may be <code>null</code> or empty.
    */
   private String m_root = null;
   
   /**
    * The sites IP address, may be <code>null</code> or empty.
    */
   private String m_ipAddress = null;
   
   /**
    * The sites port, may be <code>null</code> or empty.
    */
   private String m_port = null;
   
   /**
    * The sites FTP user name, may be <code>null</code> or empty.
    */
   private String m_userId = null;
   
   /**
    * The sites FTP password, may be <code>null</code> or empty.
    */
   private String m_password = null;
   
   /**
    * The sites status, may be <code>null</code> or empty.
    */
   private String m_state = null;
   
   /**
    * The sites folder root, may be <code>null</code> or empty.
    */
   private String m_folderRoot = null;
   
   /**
    * The sites navigation theme, may be <code>null</code> or empty.
    */
   private String m_navTheme = null;
   
   /**
    * The sites global template, may be <code>null</code> or empty.
    */
   private String m_globalTemplate = null;

   private boolean isPageBased = false;
   /**
    * Constant to indicate the inactive site state.
    */
   public static final int STATE_INACTIVE = 0;
   
   /**
    * Constant to indicate the active site state.
    */
   public static final int STATE_ACTIVE = 1;
   
   /**
    * An array with all valid state values.
    */
   public static final int[] VALID_STATES =
   {
      STATE_INACTIVE,
      STATE_ACTIVE
   };

   /**
    * The resource to query sites.
    */
   public static final String GET_SITE_RESOURCE = "sys_pubSites/getSites";
   
   /**
    * The parameter name used to pass the site name as HTML parameter
    * to the {@link #GET_SITE_RESOURCE} query resource. 
    */
   public static final String SYS_SITENAME = "sys_sitename";
   
   /**
    * The parameter name used to pass the folder root as HTML parameter
    * to the {@link #GET_SITE_RESOURCE} query resource. 
    */
   public static final String SYS_FOLDERROOT = "sys_folderroot";

   /**
    * The parameter name used to pass the site id as HTML parameter
    * to the {@link #GET_SITE_RESOURCE} query resource. 
    */
   public static final String SYS_SITEID = IPSHtmlParameters.SYS_SITEID;
   
   /**
    * This is a list of all possible HTML parameters for 
    * {@link #GET_SITE_RESOURCE} query resource.
    */
   public static final String[] ALL_PARAMS = {
         SYS_SITENAME, SYS_FOLDERROOT, SYS_SITEID};
   
   /**
    * The XML document node name.
    */
   public static final String XML_NODE_NAME = "PSXSite";
   
   // XML element and attribute names
   private static final String NAME_ATTR = "name";
   private static final String DESCRIPTION_ATTR = "description";
   private static final String BASEURL_ATTR = "baseUrl";
   private static final String ROOT_ATTR = "root";
   private static final String IPADDRESS_ATTR = "ipAddress";
   private static final String PORT_ATTR = "port";
   private static final String USERID_ATTR = "userId";
   private static final String PASSWORD_ATTR = "password";
   private static final String STATE_ATTR = "state";
   private static final String FOLDERROOT_ATTR = "folderRoot";
   private static final String NAVTHEME_ATTR = "navTheme";
   private static final String GLOBALTEMPLATE_ATTR = "globalTemplate";
   private static final String IS_PAGE_BASED_ATTR ="isPageBased";
}
