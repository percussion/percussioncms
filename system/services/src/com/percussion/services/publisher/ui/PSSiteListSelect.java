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
package com.percussion.services.publisher.ui;

import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.sitemgr.data.PSSite;
import com.percussion.services.sitemgr.data.PSSiteProperty;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A managed bean for JSF front end for site selection.
 * 
 * @author vamsinukala
 * 
 */
public class PSSiteListSelect
{
   /**
    * an iterator for site list ui. This has the action methods for the
    * following:
    * <li>Copy Site</li>
    * <li>New Site</li>
    * <li>Delete Site</li>
    * 
    * @author vamsinukala
    * 
    */
   public class SiteEntry
   {
      /**
       * the site id
       */
      private long m_id;

      /**
       * the name of the site
       */
      @SuppressWarnings("hiding")
      private String m_name;

      /**
       * the root folder for this site entry
       */
      @SuppressWarnings("hiding")
      private String m_folderRoot;

      /**
       * dummy ctor 
       * todo - remove this after testing
       * @param sitename
       * @param folderRoot
       * @param site_id
       */
      public SiteEntry(String sitename, String folderRoot, long site_id)
      {
         m_id = site_id;
         m_name = sitename;
         m_folderRoot = folderRoot;

      }
      /**
       * CTOR
       * 
       * @param site site as an entry item for the jsp
       */
      public SiteEntry(IPSSite site)
      {
         if (site == null)
         {
            throw new IllegalArgumentException("site may not be null");
         }
         m_id = site.getSiteId();
         m_name = site.getName();
         m_folderRoot = site.getFolderRoot();

      }

      /**
       * Get the cms repository folder for this site
       * 
       * @return the folder root
       */
      public String getFolderRoot()
      {
         return m_folderRoot;
      }

      /**
       * set the root folder in the cms repository for this site
       * 
       * @param folderRoot
       */
      public void setFolderRoot(String folderRoot)
      {
         this.m_folderRoot = folderRoot;
      }

      /**
       * get the name of the site
       * 
       * @return the site name
       */
      public String getName()
      {
         return m_name;
      }

      /**
       * set the name for the site
       * 
       * @param name the site name, never <code>null</code> or empty
       */
      public void setName(String name)
      {
         this.m_name = name;
      }

      /**
       * returns the site id
       * 
       * @return the site id
       */
      public long getId()
      {
         return m_id;
      }
      
      /**
       * Action listener that is fired if a site entry is edited
       * 
       * @return the name of the outcome
       */
      public String edit() throws PSNotFoundException {
         String name = getName();
         System.out.println("Editing this site with id:" +name);
         
         IPSSite s = ms_siteMgr.loadSite(name);
         setCurrent(s);
         return "edit";
      }

   }

   
   /**
    * an iterator for site list ui. This has the action methods for the
    * following:
    * <li>Copy Site</li>
    * <li>New Site</li>
    * <li>Delete Site</li>
    * 
    * @author vamsinukala
    * 
    */
   public class SiteProperty
   {
      /**
       * the site property id
       */
      private long m_id;

      /**
       * the name of the site property
       */
      @SuppressWarnings("hiding")
      private String m_name;

      /**
       * the value for this site property
       */
      @SuppressWarnings("hiding")
      private String m_value;

      /**
       * dummy ctor 
       * todo - remove this after testing
       * @param name
       * @param val
       * @param p_id
       */
      public SiteProperty(String name, String val, long p_id)
      {
         m_id    = p_id;
         m_name  = name;
         m_value = val;

      }
      
      
      /**
       * CTOR
       * 
       * @param sp site property
       */
      public SiteProperty(PSSiteProperty sp)
      {
         if (sp == null)
         {
            throw new IllegalArgumentException("site may not be null");
         }
         m_id = sp.getGUID().longValue();
         m_name = sp.getName();
         m_value = sp.getValue();
      }

      /**
       * Get the cms repository folder for this site
       * 
       * @return the value for the property
       */
      public String getValue()
      {
         return m_value;
      }

      /**
       * set the root folder in the cms repository for this site
       * 
       * @param v the value for the property
       */
      public void setValue(String v)
      {
         this.m_value = v;
      }

      /**
       * get the name of the site property
       * 
       * @return the site property name
       */
      public String getName()
      {
         return m_name;
      }

      /**
       * set the name for the site property
       * 
       * @param name the site property name
       */
      public void setName(String name)
      {
         this.m_name = name;
      }

      /**
       * returns the site property id
       * 
       * @return the site id
       */
      public long getId()
      {
         return m_id;
      }
      
      /**
       * Action listener that is fired if a site property is removed
       * 
       * @return the name of the outcome
       */
      public String remove()
      {
         String name = getName();
         System.out.println("Editing this site property:" +name);
         return "remove";
      }

   }

   /**
    * the site name
    */
   private String m_name;

   /**
    * the site description
    */
   private String m_description;

   /**
    * the site folder root as in the repository
    */
   private String m_folderRoot;

   /**
    * the current global template that the site uses
    */
   private String m_globalTemplate;

   /**
    * the global template list for this site
    */
   private List<SelectItem> m_globalTemplateList = new ArrayList<>();

   /**
    * the site path on the delivery end
    */
   private String m_sitePath;

   /**
    * the current nav theme for the site
    */
   private String m_navTheme;

   /**
    * the list of navthemes applicable to this site
    */
   private static Map<String, String> m_navThemeList = null;

   /**
    * the delivery ftp location
    */
   private String m_ipAddress;

   /**
    * the delivery ftp port
    */
   private Integer m_ftpPort;

   /**
    * the user name
    */
   private String m_userName;

   /**
    * the password for the user name to login into the ftp
    */
   private String m_password;

   /**
    * the current namespace
    */
   private String m_nameSpace;

   /**
    * the home site address for accessing after publishing to the site
    */
   private String m_baseURL;
   
   /**
    * the assembler site properties as a name, value pair
    */
   private Map<String, String> m_properties = new HashMap<>();

   /**
    * the current selection from the list of sites displayed
    */
   private IPSSite m_current;

   /**
    * the site manager
    */
   private static IPSSiteManager ms_siteMgr = PSSiteManagerLocator
         .getSiteManager();

   /**
    * the assembly service
    */
   private static IPSAssemblyService ms_asmSvc = PSAssemblyServiceLocator
         .getAssemblyService();

   /**
    * the list of site properties for the current site
    */
   private List<SiteProperty> m_siteProps = new ArrayList<>();
   /**
    * default ctor
    * 
    */
   public PSSiteListSelect()
   {

   }

   /**
    * @return the current
    */
   public IPSSite getCurrent()
   {
      return m_current;
   }

   
   /**
    * Make an internal query to get the list of nav themes in the repository
    * @return map of nav themes
    * @throws Exception
    */
   protected static Map<String, String> queryForNavThemes() throws Exception
   {
      Map<String, String> navThemesList = new HashMap<>();
//      String url = "http://localhost:9992/Rhythmyx/sys_ceSupport/lookup";
      Map<String, String> reqParams = new HashMap<>();
      reqParams.put("key", "331");
//      PSRequest req = (PSRequest) PSRequestInfo
//            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
//      PSInternalRequest ireq = PSServer.getInternalRequest(url, req, reqParams,
//            false, null);
//      Document doc = ireq.getResultDoc();
      String docStr = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
            + "<sys_Lookup>" + "<PSXEntry default=\"no\" sequence=\"\">"
            + "<PSXDisplayText>Enterprise Investments</PSXDisplayText> "
            + "<Value>ei</Value> " + "</PSXEntry>"
            + "<PSXEntry default=\"no\" sequence=\"\">"
            + "<PSXDisplayText>Corporate Investments</PSXDisplayText> "
            + "<Value>ci</Value> " + "</PSXEntry>"
            + "<PSXEntry default=\"no\" sequence=\"1\">"
            + "<PSXDisplayText>None</PSXDisplayText> " + "<Value>none</Value> "
            + "</PSXEntry>" + "</sys_Lookup>";
      StringReader docStringReader = new StringReader(docStr.toString());
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument(docStringReader,
            false);
      if (doc != null)
      {
         Element root = doc.getDocumentElement();
         if (root != null)
         {
            NodeList nl = root.getElementsByTagName("PSXEntry");
            int sz = nl.getLength();
            for (int i = 0; i < sz; i++)
            {
               Element lookup = (Element) nl.item(i);
               NodeList keyList = lookup.getElementsByTagName("PSXDisplayText");
               NodeList valList = lookup.getElementsByTagName("Value");
               if (keyList.getLength() == 1 && valList.getLength() == 1)
                  navThemesList.put(keyList.item(0).getTextContent(),
                        valList.item(0).getTextContent());
            }
         }
      }
      return navThemesList;
   }
   

   
   /**
    * A site list for GUI
    * @return Returns the entries.
    * @throws PSSiteManagerException
    */
   public List<SiteEntry> getEntries() throws PSNotFoundException {
      List<SiteEntry> rval = new ArrayList<>();
      List<IPSSite> sites = ms_siteMgr.loadSitesModifiable();
      for (IPSSite s : sites)
      {
         SiteEntry newentry = new SiteEntry(s);
         rval.add(newentry);
      }
      
      return rval;
   }

   
   /**
    * @param s the current to set
    */

   public void setCurrent(IPSSite s)
   {
      this.m_current = s;
      
      
      
      // get navtheme list
      if (m_navThemeList  == null)
      {
         try
         {
            m_navThemeList = queryForNavThemes();
         }
         catch (Exception e)
         {
            // todo
            e.printStackTrace();
         }
      }
      // Get global templates
      Set<IPSAssemblyTemplate> globalTmps = null;
      if ( m_globalTemplateList.size() == 0 )
      {
         try
         {
            globalTmps = ms_asmSvc.findAllGlobalTemplates();
         }
         catch (PSAssemblyException e)
         {
            // todo
            e.printStackTrace();
         }
         if (globalTmps != null)
         {
            for (IPSAssemblyTemplate t : globalTmps)
            {
               m_globalTemplateList.add(new SelectItem(t.getName()));
            }
         }
      }
      
      // set the rest of the properties
      if ( m_current != null )
      {
         setName(m_current.getName());
         setDescription(m_current.getDescription());
         setFolderRoot(m_current.getFolderRoot());
         setBaseURL(m_current.getBaseUrl());
         setGlobalTemplate(m_current.getGlobalTemplate());
         setSitePath(m_current.getRoot());
         
         // hidden fields on the ui by default
         setNavTheme(m_current.getNavTheme());
         setIpAddress(m_current.getIpAddress());
         setFtpPort(m_current.getPort());
         setUserName(m_current.getUserId());
         setPassword(m_current.getPassword());
         setNameSpace(m_current.getAllowedNamespaces());
      }  
      
      //    set site properties for the editor      
      HashMap<String, String> props = new HashMap<>();
      if ( m_current != null )
      {
         Set<PSSiteProperty> propSet = ((PSSite)m_current).getProperties();
         for (PSSiteProperty p : propSet)
            props.put(p.getName(), p.getValue());
         setProperties(props);
      }
   }

   // SETTERS AND GETTERS
   /**
    * @return the description
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * @param description the description to set, may not be <code>null</code> 
    * or empty
    */
   public void setDescription(String description)
   {
      if (StringUtils.isBlank(description))
         throw new IllegalArgumentException(
               "description may not be null or empty");
      this.m_description = description;
   }

   /**
    * @return the folderRoot
    */
   public String getFolderRoot()
   {
      return m_folderRoot;
   }

   /**
    * @param folderRoot the folderRoot to set, may not be <code>null</code> 
    * or empty
    */
   public void setFolderRoot(String folderRoot)
   {
      if (StringUtils.isBlank(folderRoot))
         throw new IllegalArgumentException(
               "folder root may not be null or empty");
      this.m_folderRoot = folderRoot;
   }

   /**
    * @return the ftpAddress
    */
   public String getIpAddress()
   {
      return m_ipAddress;
   }

   /**
    * @param ftpAddress the ftpAddress to set may not be <code>null</code> 
    * or empty
    */
   public void setIpAddress(String ftpAddress)
   {
      if (StringUtils.isBlank(ftpAddress))
         throw new IllegalArgumentException(
               "ftpAddress may not be null or empty");
      this.m_ipAddress = ftpAddress;
   }

   /**
    * @return the ftpPort
    */
   public Integer getFtpPort()
   {
      return m_ftpPort;
   }

   /**
    * @param ftpPort the ftpPort to set must be a positive number
    */
   public void setFtpPort(Integer ftpPort)
   {
      if ( ftpPort <= 0 )
            throw new IllegalArgumentException(
                  "ftpAddress may not be 0 or negative");
      this.m_ftpPort = ftpPort;
   }

   /**
    * @return the globalTemplate
    */
   public String getGlobalTemplate()
   {
      return m_globalTemplate;
   }

   /**
    * @param globalTemplate the globalTemplate to set, may or may 
    * not be <code>null</code>
    * 
    */
   public void setGlobalTemplate(String globalTemplate)
   {
      this.m_globalTemplate = globalTemplate;
   }

   /**
    * @return the globalTemplateList of id, name
    */
   public List<SelectItem> getGlobalTemplates()
   {
      return m_globalTemplateList;
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * @param name the name to set for the site may not be <code>null</code> 
    * or empty
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      this.m_name = name;
   }

   /**
    * get the home site address
    * @return the url as a string
    */
   public String getBaseURL()
   {
      return m_baseURL;
   }
   
   /**
    * set the home site address
    * @param u
    */
   public void setBaseURL(String u)
   {
      this.m_baseURL = u;
   }
   /**
    * @return the nameSpace
    */
   public String getNameSpace()
   {
      return m_nameSpace;
   }

   /**
    * @param nameSpace the nameSpace to set may not be <code>null</code> 
    * or empty
    */
   public void setNameSpace(String nameSpace)
   {
      if (StringUtils.isBlank(nameSpace))
         nameSpace = "";

      this.m_nameSpace = nameSpace;
   }

   /**
    * @return the navTheme
    */
   public String getNavTheme()
   {
      return m_navTheme;
   }

   /**
    * @param navTheme the navTheme to set, may be empty or <code>null</code>
    */
   public void setNavTheme(String navTheme)
   {
      this.m_navTheme = navTheme;
   }

   /**
    * @return the navThemeList
    */
   public Map<String, String> getNavThemeList()
   {
      return m_navThemeList;
   }

   
   /**
    * @return the password
    */
   public String getPassword()
   {
      return m_password;
   }

   /**
    * @param password the password to set may not be <code>null</code> or empty
    */
   public void setPassword(String password)
   {
      if (StringUtils.isBlank(password))
         password="";
      this.m_password = password;
   }

   /**
    * Gather the site properties for display based on the current selection
    * if the current selection is null, then return an empty list.
    * @return the list of site properties
    */
   public List<SiteProperty> getSiteProperties()
   {
      if ( m_current != null )
      {
         m_siteProps.clear();
         Set<PSSiteProperty> props = ((PSSite)m_current).getProperties();
         for (PSSiteProperty p : props)
         {
            SiteProperty sp = new SiteProperty(p);
            m_siteProps.add(sp);
         }
      }
      return m_siteProps;
   }
   
   
   /**
    * @return the properties
    */
   public Map<String, String> getProperties()
   {
      return m_properties;
   }

   /**
    * @param properties the properties to set
    */
   public void setProperties(Map<String, String> properties)
   {
      this.m_properties = properties;
   }

   /**
    * @return the sitePath
    */
   public String getSitePath()
   {
      return m_sitePath;
   }

   /**
    * @param sitePath the sitePath to set may not be <code>null</code> or empty
    */
   public void setSitePath(String sitePath)
   {
      if (StringUtils.isBlank(sitePath))
         throw new IllegalArgumentException("sitePath may not be null or empty");
      this.m_sitePath = sitePath;
   }

   /**
    * @return the userName
    */
   public String getUserName()
   {
      return m_userName;
   }

   /**
    * @param userName the userName to set may not be <code>null</code> or empty
    */
   public void setUserName(String userName)
   {
      if (StringUtils.isBlank(userName))
         userName = "";
      this.m_userName = userName;
   }

   /**
    * gather the data from the page onto the <b>current</b>
    * @param s the current site
    * @return return the modified site (same as the input)
    */
   private IPSSite copyEditedSiteData(IPSSite s)
   {
      if ( s != null )
      {
         s.setName(getName());
         s.setDescription(getDescription());
         s.setFolderRoot(getFolderRoot());
         s.setBaseUrl(getBaseURL());
         s.setGlobalTemplate(getGlobalTemplate());
         

         s.setRoot(getSitePath());
         
         // hidden fields on the ui by default
         s.setNavTheme(getNavTheme());
         s.setIpAddress(getIpAddress());
         s.setPort(getFtpPort());
         s.setUserId(getUserName());
         s.setPassword(getPassword());
         s.setAllowedNamespaces(getNameSpace());  
         Map<String, String> pMap = getProperties();
         Set<String> keys = pMap.keySet();
         for (String k : keys)
         {
            System.out.println("Edited Property ["+k+","+pMap.get(k)+"]");
         }
      }
      return s;
   }
   
   public String save()
   { 
      copyEditedSiteData(getCurrent());
      IPSSite s = getCurrent();
      ms_siteMgr.saveSite(s);
      return "save";
   }
   
   
   @Override
   public boolean equals(Object arg0)
   {
      return EqualsBuilder.reflectionEquals(this, arg0);
   }

   /**
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /**
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
}
