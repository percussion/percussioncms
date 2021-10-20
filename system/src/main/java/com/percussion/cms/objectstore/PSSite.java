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
   public Object clone()
   {
      PSSite clone = (PSSite) super.clone();
      clone.copyFrom(this);
      
      return clone;
   }
   
   /**
    * Must be overridden to properly fulfill the contract.
    *
    * @return a value computed by adding the hash codes of all required members.
    */
   public int hashCode()
   {
      return m_name.hashCode();
   }
   
   /**
    * Tests if the supplied object is equal to this one.
    * 
    * @param o the object to test, may be <code>null</code>.
    * @return <code>true</code> if the supplied object is equal to this one,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSSite))
         return false;

      PSSite t = (PSSite) o;
      if (t.m_id != m_id)
         return false;
      if (!compare(t.m_name, m_name))
         return false;
      if (!compare(t.m_description, m_description))
         return false;
      if (!compare(t.m_root, m_root))
         return false;
      if (!compare(t.m_ipAddress, m_ipAddress))
         return false;
      if (!compare(t.m_port, m_port))
         return false;
      if (!compare(t.m_userId, m_userId))
         return false;
      if (!compare(t.m_password, m_password))
         return false;
      if (!compare(t.m_state, m_state))
         return false;
      if (!compare(t.m_folderRoot, m_folderRoot))
         return false;
      if (!compare(t.m_navTheme, m_navTheme))
         return false;
      if (!compare(t.m_globalTemplate, m_globalTemplate))
         return false;

      return true;
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
}
