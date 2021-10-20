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
package com.percussion.design.objectstore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Implementation of the interface {@link IPSJavaPlugin}
 */
public class PSJavaPlugin implements IPSJavaPlugin
{
   /**
    * Ctor taking all the attributes required for the object creation.
    * @param osKey string key representing the operating system of the client,
    * must not be <code>null</code> or empty.
    * @param browserKey string key representing the browser of the client,
    * must not be <code>null</code> or empty.
    * @param versionToUse string key representing the plugin version to use,
    * must not be <code>null</code> or empty and conform to the plugin version
    * syntax, i.e. 1.4.1_mn.
    * @param isStatic <code>true</code> to use static versioning type and
    * <code>false</code> otherwise.
    * @param downloadLocation location URL of the plugin executable/cab file to
    * download to the client's machine in case required, must not be <code>null</code>
    * and must be a HTTP URL for remote download or a URL inapplet syntax,
    * for example,<e> ../rx_resources/AppletJars/plugin.exe</e>.
    * @throws IllegalArgumentException if any of the required parameters is
    * invalid.
    */
   public PSJavaPlugin(String osKey, String browserKey, String versionToUse,
      boolean isStatic, String downloadLocation) throws IllegalArgumentException
   {
      if(osKey==null || osKey.trim().length()<1)
         throw new IllegalArgumentException("osKey must not be null or empty");
      if(browserKey==null || browserKey.trim().length()<1)
         throw new IllegalArgumentException("browserKey must not be null or empty");
      if(versionToUse==null || versionToUse.trim().length()<1)
         throw new IllegalArgumentException("versionToUse must not be null or empty");
      if(downloadLocation==null || downloadLocation.trim().length()<1)
         throw new IllegalArgumentException("downloadLocation must not be null or empty");

      m_osKey = osKey.trim();
      m_browserKey = browserKey.trim();
      m_versionToUse = versionToUse.trim();
      if(isStatic)
         m_versioningType = VERSIONING_TYPE_STATIC;
      m_downloadLocation = downloadLocation.trim();
   }

   /**
    * Another version of the ctor that takes the HTTP USER AGENT string instaed
    * of osKey and browserKey.
    * @param httpUserAgent string key representing the httpUserAgent, must not
    * be <code>null</code> or empty.
    * @param versionToUse string key representing the plugin version to use,
    * must not be <code>null</code> or empty and conform to the plugin version
    * syntax, i.e. 1.4.1_mn.
    * @param isStatic <code>true</code> to use static versioning type and
    * <code>false</code> otherwise.
    * @param downloadLocation location URL of the plugin executable/cab file to
    * download to the client's machine in case required, must not be <code>null</code>
    * and must be a HTTP URL for remote download or a URL inapplet syntax,
    * for example,<e> ../rx_resources/AppletJars/plugin.exe</e>.
    * @throws IllegalArgumentException if any of the required parameters is
    * invalid.
    */
   public PSJavaPlugin(String httpUserAgent, String versionToUse,
      boolean isStatic, String downloadLocation) throws IllegalArgumentException
   {
      if(httpUserAgent==null || httpUserAgent.trim().length()<1)
         throw new IllegalArgumentException(
            "httpUserAgent must not be null or empty");

      String osKey = PSJavaPluginConfig.getOSKeyFromUserAgent(httpUserAgent);
      if(osKey==null || osKey.trim().length()<1)
         throw new IllegalArgumentException("osKey must not be null or empty");

      String browserKey = PSJavaPluginConfig.getBrowserKeyFromUserAgent(
         httpUserAgent);
      if(browserKey==null || browserKey.trim().length()<1)
         throw new IllegalArgumentException(
            "browserKey must not be null or empty");

      if(versionToUse==null || versionToUse.trim().length()<1)
         throw new IllegalArgumentException(
            "versionToUse must not be null or empty");
      if(downloadLocation==null || downloadLocation.trim().length()<1)
         throw new IllegalArgumentException(
            "downloadLocation must not be null or empty");

      m_osKey = osKey.trim();
      m_browserKey = browserKey.trim();
      m_versionToUse = versionToUse.trim();
      if(isStatic)
         m_versioningType = VERSIONING_TYPE_STATIC;
      m_downloadLocation = downloadLocation.trim();
   }
   /**
    * Ctor taking the source element.
    * @param elemSrc must not be <code>null</code> or empty and must conform to
    * the DTD specified in the class description.
    * @throws PSUnknownNodeTypeException if source XML element fails to conform
    * to the DTD.
    * @throws IllegalArgumentException if any of the required attributes is
    * missing for object construction.
    */
   public PSJavaPlugin(Element elemSrc)
      throws PSUnknownNodeTypeException, IllegalArgumentException
   {
      fromXml(elemSrc, null, null);
   }

   /**
    * Set the osKey.
    * @param osKey to set, must not be <code>null</code> or empty and must be
    * one of the supported OS types.
    */
   public void setOsKey(String osKey)
   {
      if(osKey==null || osKey.trim().length()<1)
         throw new IllegalArgumentException("osKey must not be null or empty");

      m_osKey = osKey.trim();
   }

   /**
    * Set the browserKey.
    * @param browserKey to set, must not be <code>null</code> or empty and must
    * be one of the supported browser types.
    * @throws IllegalArgumentException if browserKey is <code>null</code> or empty
    * or is not one of the supported browsers.
    */
   public void setBrowserKey(String browserKey)
   {
      if(browserKey==null || browserKey.trim().length()<1)
         throw new IllegalArgumentException("browserKey must not be null or empty");

      m_browserKey = browserKey.trim();
   }

   /**
    * Set the plugin version to use.
    * @param versionToUse version to set, must not be <code>null</code> or empty
    *  and must follow the required syntax.
    * @throws IllegalArgumentException if version is <code>null</code> or empty
    *    or does not follow the requires syntax, i.e. 1.4.1_01
    */
   public void setVersionToUse(String versionToUse)
   {
      if(versionToUse==null || versionToUse.trim().length()<1)
         throw new IllegalArgumentException("versionToUse must not be null or empty");

      m_versionToUse = versionToUse.trim();
   }

   /**
    * Set the plugin download location.
    * @param downloadLocation download location, must not be <code>null</code>
    *    or empty.
    * @throws IllegalArgumentException if version is <code>null</code> or empty.
    */
   public void setDownloadLocation(String downloadLocation)
   {
      if(downloadLocation==null || downloadLocation.trim().length()<1)
         throw new IllegalArgumentException("downloadLocation must not be null or empty");

      m_downloadLocation = downloadLocation.trim();
   }

   /**
    * Set the plugin versioning type to static or dynamic
    * @param isStatic <code>true</code> to set it versioning type to
    * VERSIONING_TYPE_STATIC or <code>false</code> for VERSIONING_TYPE_DYNAMIC
    * type.
    * @throws IllegalArgumentException if type specified is not from the allowed
    * ones.
    */
   public void setStaticVersioningType(boolean isStatic)
   {
      if(isStatic)
         m_versioningType = VERSIONING_TYPE_STATIC;
      else
         m_versioningType = VERSIONING_TYPE_DYNAMIC;
   }

   //Implementation of the intreface method
   public String getOsKey()
   {
      return m_osKey;
   }

   //Implementation of the intreface method
   public String getBrowserKey()
   {
      return m_browserKey;
   }

   //Implementation of the intreface method
   public String getVersionToUse()
   {
      return m_versionToUse;
   }

   //Implementation of the intreface method
   public String getVersioningType()
   {
      return m_versioningType;
   }

   //Implementation of the intreface method
   public String getDownloadLocation()
   {
      return m_downloadLocation;
   }

   //Implementation of the intreface method
   public boolean isStaticVersioning()
   {
      return m_versioningType.equalsIgnoreCase(VERSIONING_TYPE_STATIC);
   }

   //Implementation of the interface method
   public void fromXml(Element sourceNode, 
      @SuppressWarnings("unused") IPSDocument parentDoc, 
      @SuppressWarnings("unused") List parentComponents)
      throws PSUnknownNodeTypeException
   {
      if(sourceNode == null)
         throw new IllegalArgumentException("sourceNode must not be null");

      if(!sourceNode.getTagName().equals(XML_NODE_NAME))
      {
         String[] args= {XML_NODE_NAME, sourceNode.getTagName()};
         throw new PSUnknownNodeTypeException(1001, args);
      }

      m_osKey = sourceNode.getAttribute(ATTR_OSKEY);

      if(m_osKey.trim().length()<1) {
         Object[] args = { XML_NODE_NAME, ATTR_OSKEY, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      m_browserKey = sourceNode.getAttribute(ATTR_BROWSERKEY);
      if(m_browserKey.trim().length()<1) {
         Object[] args = { XML_NODE_NAME, ATTR_BROWSERKEY, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      m_versionToUse = sourceNode.getAttribute(ATTR_VERSIONTOUSE);
      if(m_versionToUse.trim().length()<1){
         Object[] args = { XML_NODE_NAME, ATTR_VERSIONTOUSE, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      m_versioningType = sourceNode.getAttribute(ATTR_VERSIONINGTYPE);
      if(m_versioningType.trim().length()<1){
         Object[] args = { XML_NODE_NAME, ATTR_VERSIONINGTYPE, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      m_downloadLocation = sourceNode.getAttribute(ATTR_DOWNLOADLOCATION);
      if(m_downloadLocation.trim().length()<1){
         Object[] args = { XML_NODE_NAME, ATTR_DOWNLOADLOCATION, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
   }

   //Implementation of the intreface method
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc must not be null");
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(ATTR_OSKEY, m_osKey);
      root.setAttribute(ATTR_BROWSERKEY, m_browserKey);
      root.setAttribute(ATTR_VERSIONTOUSE, m_versionToUse);
      root.setAttribute(ATTR_VERSIONINGTYPE, m_versioningType);
      root.setAttribute(ATTR_DOWNLOADLOCATION, m_downloadLocation);

      return root;
   }

   @Override
   public boolean equals( Object o )
   {
      if ( !(o instanceof PSJavaPlugin ))
         return false;

      PSJavaPlugin obj2 = (PSJavaPlugin) o;

      return m_osKey.equals(obj2.m_osKey) &&
          m_browserKey.equals(obj2.m_browserKey) &&
          m_versionToUse.equals(obj2.m_versionToUse) &&
          m_versioningType.equals(obj2.m_versioningType) &&
          m_downloadLocation.equals(obj2.m_downloadLocation);
   }
   
  
   @Override
   public int hashCode()
   {
      return
            hashCodeOr0(m_osKey)
            + hashCodeOr0(m_browserKey) 
            + hashCodeOr0(m_versionToUse)
            + hashCodeOr0(m_versioningType)
            + hashCodeOr0(m_downloadLocation);
   }

   /**
    * Hash code of the provided object or 0 if the object is <code>null</code>.
    */
   private int hashCodeOr0(Object object)
   {
      return object == null ? 0 : object.hashCode();
   }

   public String getStaticClsid()
   {
      StringTokenizer st = new StringTokenizer(m_versionToUse, ".");
      String majorVersion = "";
      int count = 0;
      while (st.hasMoreTokens())
      {
          String token = st.nextToken();
          majorVersion += token;
          count++;
          if (count > 1)
             break;
      }
      
      String clsid = ms_familyClassIds.get(majorVersion);
      if (clsid == null)
      {
         throw new RuntimeException("Unsupported static Java plugin version " +
                "specified: " + m_versionToUse);
      }
      
      return clsid;
   }

   /**
    * See IPSComponent.
    * @return id
    */
   public int getId()
   {
      throw new UnsupportedOperationException("getId is not implemented");
   }

   /**
    * See IPSComponent.
    * @param id
    */
   public void setId(@SuppressWarnings("unused") int id)
   {
      throw new UnsupportedOperationException("setId is not implemented");
   }

   /**
    * See IPSComponent.
    * @param cxt
    * @throws PSSystemValidationException
    */
   @SuppressWarnings("unused")
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      throw new UnsupportedOperationException("validate is not implemented");
   }


   @Override
   public Object clone()
   {
      throw new UnsupportedOperationException("clone is not implemented");
   }

   /**
    *
    */
   private String m_osKey = "";
   /**
    *
    */
   private String m_browserKey = "";
   /**
    *
    */
   private String m_versionToUse = "";
   /**
    *
    */
   private String m_versioningType = VERSIONING_TYPE_DYNAMIC;
   /**
    *
    */
   private String m_downloadLocation = "";
   
   /**
    * Map of supported java versions to family class ids.  Where version is the
    * first two parts without the "." separator, e.g. JDK 1.5 is "15".  Never
    * <code>null</code> or empty after initialization.
    */
   private static Map<String, String> ms_familyClassIds = 
      new HashMap<>();
   
   static
   {
      ms_familyClassIds.put("15", CLASSID_STATIC_15);
      ms_familyClassIds.put("16", CLASSID_STATIC_16);
   }
}
