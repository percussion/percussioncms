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

package com.percussion.deployer.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.rx.config.IPSConfigService;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for all descriptors used to run deployment jobs.
 */
public abstract class PSDescriptor  implements IPSDeployComponent
{
   /**
    * Constructs this descriptor specifying the name.
    * 
    * @param name The name, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    */
   protected PSDescriptor(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      
      m_name = name;
   }
   
   /**
    * Construct this object from its XML representation.  See 
    * {@link #toXml(Document)} for the format expected.
    * 
    * @param src The source XML element, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>src</code> is <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>src</code> is malformed.
    */
   protected PSDescriptor(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
         
      fromXml(src);
   }
   
   /**
    * Parameterless ctor for use by derived classes only.
    */
   protected PSDescriptor()
   {
   }
   
   /**
    * Sets the name of this descriptor.
    * 
    * @param name The name, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
         
      m_name = name;
   }

   /**
    * Gets the name of this descriptor.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /** 
    * Sets the description for this object.
    * 
    * @param desc The description, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>desc</code> is invalid.
    */
   public void setDescription(String description)
   {
      if (description == null)
         throw new IllegalArgumentException(
            "description may not be null");
      
      m_description = description;
   }
   
   /**
    * Get the description for this object. 
    * 
    * @return The description, never <code>null</code>, may be empty.
    */
   public String getDescription()
   {
      return m_description;
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXDescriptor (Description, Version, Publisher, CmsVersion, 
    * ConfigFile, PKGDependencies)>
    * &lt;!ATTLIST PSXDescriptor
    *    name CDATA #REQUIRED
    *    id CDATA #REQUIRED
    * >
    * &lt;!ELEMENT ImplConfigFile EMPTY>
    * &lt;!ELEMENT LocalConfigFile EMPTY>
    * &lt;!ELEMENT Description (#PCDATA)>
    * &lt;!ELEMENT Publisher EMPTY>
    * &lt;!ATTLIST Publisher
    *   name CDATA #REQUIRED
    *   url CDATA #REQUIRED
    * >
    * &lt;!ELEMENT Version EMPTY>
    * &lt;!ELEMENT CmsVersion EMPTY>
    * &lt;!ATTLIST CmsVersion
    *    max CDATA #REQUIRED
    *    min CDATA #REQUIRED
    * >
    * &lt;!ELEMENT PKGDependencies EMPTY (PKGDependency)>
    * &lt;!ELEMENT PKGDependency EMPTY>
    * &lt;!ATTLIST PKGDependency
    *   name CDATA #REQUIRED
    *   minVersion CDATA
    *   manVersion CDATA
    * </code></pre>
    * 
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_ID, m_id);
      PSXmlDocumentBuilder.addElement(doc, root, XML_EL_DESC, m_description);
      Element publisher = 
         PSXmlDocumentBuilder.addElement(doc, root, XML_EL_PUBLISHER, null);
      publisher.setAttribute(XML_ATTR_NAME, m_publisherName);
      publisher.setAttribute(XML_ATTR_URL, m_publisherUrl);
      Element cmsversion = 
         PSXmlDocumentBuilder.addElement(doc, root, XML_EL_CMS_VERSION, null);
      cmsversion.setAttribute(XML_ATTR_MAX, m_cmsMaxVersion);
      cmsversion.setAttribute(XML_ATTR_MIN, m_cmsMinVersion);
      PSXmlDocumentBuilder.addElement(doc, root, XML_EL_VERSION, m_version);
      PSXmlDocumentBuilder.addElement(
         doc, root, XML_EL_IMPL_CONFIG_FILE, m_configDefFile);
      PSXmlDocumentBuilder.addElement(
         doc, root, XML_EL_LOCAL_CONFIG_FILE, m_localConfigFile);
      
      //Pkg Dependencies
      Element pkgRoot = PSXmlDocumentBuilder.addElement(
            doc, root, XML_PKG_DEP_ROOT_NAME, null);
      int pkgSize = m_pkgDepList.size();

      for (int i = 0; i < pkgSize; i++)
      {
         
         Map<String, String> pkgDep = m_pkgDepList.get(i);
         
         Element pkgElement = PSXmlDocumentBuilder.addElement(
               doc, pkgRoot, XML_PKG_DEP_EL_NAME, null);
         pkgElement.setAttribute(XML_PKG_DEP_NAME, 
               pkgDep.get(XML_PKG_DEP_NAME));
         pkgElement.setAttribute(XML_PKG_DEP_VERSION, 
               pkgDep.get(XML_PKG_DEP_VERSION));
         pkgElement.setAttribute(XML_PKG_DEP_IMPLIED, 
               pkgDep.get(XML_PKG_DEP_IMPLIED));       
      }
      
      
      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See 
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
         
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      m_id = StringUtils.defaultString(sourceNode.getAttribute(XML_ATTR_ID));
      m_name = PSDeployComponentUtils.getRequiredAttribute(sourceNode, 
         XML_ATTR_NAME);
         
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      String description = tree.getElementData(XML_EL_DESC);
      if (description == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_EL_DESC);
      }
      
      m_description = description;
      setVersion(tree.getElementData(XML_EL_VERSION));
      setConfigDefFile(tree.getElementData(XML_EL_IMPL_CONFIG_FILE));
      setLocalConfigFile(tree.getElementData(XML_EL_LOCAL_CONFIG_FILE));
      Element publisher = tree.getNextElement(XML_EL_PUBLISHER);
      if(publisher != null)
      {
         setPublisherName(publisher.getAttribute(XML_ATTR_NAME));
         setPublisherUrl(publisher.getAttribute(XML_ATTR_URL));
      }
      Element cmsversion = tree.getNextElement(XML_EL_CMS_VERSION);
      if(cmsversion != null)
      {
         setCmsMaxVersion(cmsversion.getAttribute(XML_ATTR_MAX));
         setCmsMinVersion(cmsversion.getAttribute(XML_ATTR_MIN));
      }
      // Get Package Dependencies    
      PSXmlTreeWalker pkgDepTree = 
         new PSXmlTreeWalker(tree.getNextElement(XML_PKG_DEP_ROOT_NAME));
      
      Element pkgDepElem = pkgDepTree.getNextElement(XML_PKG_DEP_EL_NAME);

      while( pkgDepElem != null )
      {
         Map<String,String> pkgDepMap = new HashMap<String,String>();
         pkgDepMap.put(XML_PKG_DEP_NAME, 
               pkgDepElem.getAttribute(XML_PKG_DEP_NAME));
         pkgDepMap.put(XML_PKG_DEP_VERSION, 
               pkgDepElem.getAttribute(XML_PKG_DEP_VERSION));         
         pkgDepMap.put(XML_PKG_DEP_IMPLIED, 
               pkgDepElem.getAttribute(XML_PKG_DEP_IMPLIED));
         m_pkgDepList.add(pkgDepMap);
         
         pkgDepElem = pkgDepTree.getNextElement(XML_PKG_DEP_EL_NAME);
      }

      
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
         
      if (!(obj instanceof PSDescriptor))
         throw new IllegalArgumentException("obj wrong type");

      PSDescriptor desc = (PSDescriptor)obj;
      m_id = desc.m_id;
      m_name = desc.m_name;
      m_description = desc.m_description;
      m_version = desc.m_version;
      m_cmsMaxVersion = desc.m_cmsMaxVersion;
      m_cmsMinVersion = desc.m_cmsMinVersion;
      m_publisherName = desc.m_publisherName;
      m_publisherUrl = desc.m_publisherUrl;
      m_configDefFile = desc.m_configDefFile;    
      m_localConfigFile = desc.m_localConfigFile;
      m_pkgDepList = desc.m_pkgDepList;
      
   } 
   
   /**
    * Get the archive entry path for the type of configuration
    * file specified.
    * @param type one of the <code>IPSConfigService.ConfigTypes</code>
    * enum values.
    * @return the path, never <code>null</code>.
    */
   public static String getConfigArchiveEntryPath(IPSConfigService.ConfigTypes type)
   {
      StringBuilder sb = new StringBuilder("configurations/");
      switch(type)
      {
         case LOCAL_CONFIG:
         case DEFAULT_CONFIG:   
            sb.append("local");
            break;
         
         case CONFIG_DEF:
            sb.append("impl");
            break;
         default:
            throw new IllegalArgumentException(
               "Invalid config type specified.");
      }
      sb.append("_config.xml");
      return sb.toString();
   }
   
   /**
    * @return the publisher name
    */
   public String getPublisherName()
   {
      return m_publisherName;
   }

   /**
    * @param publisherName the publisher name to set
    */
   public void setPublisherName(String publisherName)
   {
      m_publisherName = StringUtils.defaultString(publisherName);
   }

   /**
    * @return the publisher URL
    */
   public String getPublisherUrl()
   {
      return m_publisherUrl;
   }

   /**
    * @param publisherUrl the publisher Url to set
    */
   public void setPublisherUrl(String publisherUrl)
   {
      m_publisherUrl = StringUtils.defaultString(publisherUrl);
   }

   /**
    * @return the id
    */
   public String getId()
   {
      return m_id;
   }

   /**
    * @param id the id to set
    */
   public void setId(String id)
   {
      m_id = StringUtils.defaultString(id);
   }

   /**
    * @return the version
    */
   public String getVersion()
   {
      return m_version;
   }

   /**
    * @param version the version to set, defaults to
    * 1.0.0 if value passed in is <code>null</code>, empty.
    * This value will be formatted
    * by {@link #formatVersion(String, boolean)}. 
    */
   public void setVersion(String version)
   {
      m_version = StringUtils.defaultString(
         formatVersion(version, true, true), 
         DEFAULT_VERSION);
   }

   /**
    * @return the cmsMinVersion
    */
   public String getCmsMinVersion()
   {
      return m_cmsMinVersion;
   }

   /**
    * @param cmsMinVersion the cmsMinVersion to set, defaults to
    * 6.0.0 if value passed in is <code>null</code>, empty.
    * This value will be formatted
    * by {@link #formatVersion(String, boolean)}.
    */
   public void setCmsMinVersion(String cmsMinVersion)
   {
      m_cmsMinVersion = 
         StringUtils.defaultString(
            formatVersion(cmsMinVersion, false, true),
            DEFAULT_CMS_MIN_VERSION);
   }

   /**
    * @return the cmsMaxVersion
    */
   public String getCmsMaxVersion()
   {
      return m_cmsMaxVersion;
   }

   /**
    * @param cmsMaxVersion the cmsMaxVersion to set. This value will be formatted
    * by {@link #formatVersion(String, boolean)}.
    */
   public void setCmsMaxVersion(String cmsMaxVersion)
   {
      m_cmsMaxVersion = formatVersion(cmsMaxVersion, false, true);
   }
   
   /**
    * @param pkgName the name of the package 
    * @param pkgVersion  package version allowed, defaults to
    * 1.0.0 if value passed in is <code>null</code>, empty.
    * This value will be formatted
    * by {@link #formatVersion(String, boolean, boolean)}.
    * @param pkgImplied   
    */
   public void setPkgDep(
         String pkgName, String pkgVersion, boolean pkgImplied)
   {
      Map<String,String> pkgDepMap = new HashMap<String,String>();
      pkgDepMap.put(PSDescriptor.XML_PKG_DEP_NAME, pkgName);
      pkgDepMap.put(PSDescriptor.XML_PKG_DEP_VERSION, StringUtils.defaultString(
            formatVersion(pkgVersion, false, false), DEFAULT_VERSION));      
      pkgDepMap.put(PSDescriptor.XML_PKG_DEP_IMPLIED, Boolean.toString(pkgImplied));
      
      m_pkgDepList.add(pkgDepMap);
   }
   
   /**
    * @return ArrayList of package dependency Maps
    * 
    * Map keys are:
    * XML_PKG_DEP_NAME
    * XML_PKG_DEP_VERSION
    */
   public List<Map<String, String>> getPkgDepList()
   {
      return m_pkgDepList;
   }

   /**
    * @return the configDef file
    */
   public String getConfigDefFile()
   {
      return m_configDefFile;
   }

   /**
    * @param configFile config def file to set
    */
   public void setConfigDefFile(String configFile)
   {
      m_configDefFile = StringUtils.defaultString(configFile);
   }
   
   /**
    * @return the local configFile
    */
   public String getLocalConfigFile()
   {
      return m_localConfigFile;
   }

   /**
    * @param configFile the local configFile to set
    */
   public void setLocalConfigFile(String configFile)
   {
      m_localConfigFile = StringUtils.defaultString(configFile);
   }

   // see IPSDeployComponent interface
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().
      append(m_name).
      append(m_id).
      append(m_description).
      append(m_publisherName).
      append(m_publisherUrl).
      append(m_version).
      append(m_cmsMaxVersion).
      append(m_cmsMinVersion).
      append(m_configDefFile).
      append(m_localConfigFile).
      append(m_pkgDepList).
      hashCode();
      
   }

   // see IPSDeployComponent interface
   @Override
   public boolean equals(Object obj)
   {
      if(!(obj instanceof PSDescriptor))
         return false;
      final PSDescriptor other = (PSDescriptor)obj;
      return new EqualsBuilder().
         append(m_name, other.m_name).
         append(m_id, other.m_id).
         append(m_description, other.m_description).
         append(m_publisherName, other.m_publisherName).
         append(m_publisherUrl, other.m_publisherUrl).
         append(m_version, other.m_version).
         append(m_cmsMaxVersion, other.m_cmsMaxVersion).
         append(m_cmsMinVersion, other.m_cmsMinVersion).
         append(m_configDefFile, other.m_configDefFile).
         append(m_localConfigFile, other.m_localConfigFile).
         append(m_pkgDepList, other.m_pkgDepList).
         isEquals();        
      
   }
   
   /**
    * Helper method to correctly format the version to always be in the format
    * of (n.n.n) or (n.n.n.extraText) if extra is allowed and specified in the
    * passed in version. Can also validate for malformed version formats.
    * @param version the version string, may be <code>null</code> or empty.
    * @param isExtraAllowed flag indicating that the extra text post-fix is
    * allowed in the version (n.n.n.extraText), if <code>false</code>
    * then extraText passed in will be stripped out.
    * @param allowExceptions flag indicating that exceptions will be 
    * thrown for mismatches.
    * If <code>false</code> then the version is simply corrected and no 
    * exception is thrown.
    * @return formatted version, never <code>null</code>, may be empty.    
    */
   public static String formatVersion(
      final String version, boolean isExtraAllowed, boolean allowExceptions)
   {
      final String formatErrorMsg = "Invalid version format specified.";
      if(StringUtils.isBlank(version))
         return "";
      Pattern p = Pattern.compile(
         "(\\d+)\\.{0,1}(\\d*)\\.{0,1}(\\d*)\\.{0,1}(.*)");
      StringBuilder sb = new StringBuilder();
      Matcher m = p.matcher(version);
      if(!m.matches())
      {
         if(allowExceptions)
         {  
            throw new RuntimeException(formatErrorMsg);
         }
         return "";
      }
      String maj = m.group(1);
      String min = m.group(2);
      String mic = m.group(3);
      String extra = m.group(4);
      if(maj == null)
         return "";
      
      if(allowExceptions && StringUtils.isNotBlank(extra) && 
         (StringUtils.isBlank(mic) || (!StringUtils.isNumeric(mic))))
         throw new RuntimeException(formatErrorMsg);
      sb.append(maj);
      sb.append(".");
      if(allowExceptions && StringUtils.isNotBlank(min) 
         && !StringUtils.isNumeric(min))
      {
         throw new RuntimeException(formatErrorMsg);
      }  
      if(StringUtils.isBlank(min) || !StringUtils.isNumeric(min))
      {
         sb.append("0.");
         isExtraAllowed = false;
      }
      else
      {
         sb.append(min);
         sb.append(".");
      }
      if(allowExceptions && StringUtils.isNotBlank(mic) 
         && !StringUtils.isNumeric(mic))
      {
         throw new RuntimeException(formatErrorMsg);
      } 
      if(StringUtils.isBlank(mic) || !StringUtils.isNumeric(mic))
      {
         sb.append("0");
         isExtraAllowed = false;
      }
      else
      {
         sb.append(mic);
      }
      if(isExtraAllowed && StringUtils.isNotBlank(extra))
      {
         sb.append(".");
         sb.append(extra);
      }
      return sb.toString();
      
   }  
   
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXDescriptor";

   /**
    * Name of this descriptor, never <code>null</code> or empty after ctor, 
    * may be modified by a call to {@link #setName(String)}
    */
   private String m_name;

   /**
    * The description for this object, initialized to an empty string. May be 
    * modified by calls to {@link #setDescription(String)}. Never <code>null
    * </code>
    */
   private String m_description = "";
   
   /**
    * The name of the publisher who created this descriptor. Never
    * <code>null</code>, may be empty. Initialized to an empty string.
    */
   private String m_publisherName = "";
   
   /**
    * The url of the publisher who created the descriptor. Never
    * <code>null</code>, may be empty. Initialized to an empty string. 
    */
   private String m_publisherUrl = "";
   
   /**
    * The unique id for the descriptor. Never <code>null</code>, 
    * may be empty if the archive has not yet been created.
    * Initialized to an empty string. 
    */
   private String m_id = "";
   
   /**
    * The version of the descriptor. Expected to follow the
    * form of (n.n.n.text) and is required. Never <code>null</code>
    * or empty. Defaults to 1.0.0.
    */
   private String m_version = DEFAULT_VERSION;
   
   /**
    * The minimum CMS version that the bundle can be installed to.
    * Never <code>null</code> or empty. Defaults to 6.6.0.
    */
   private String m_cmsMinVersion = DEFAULT_CMS_MIN_VERSION;
   
   /**
    * The maximum CMS version that the bundle can be installed to.
    * Never <code>null</code>, may be empty in which case any CMS
    * version will work as the max.
    */
   private String m_cmsMaxVersion = "";
   
   /**
    * The implementor configuration file that is included in this bundle.
    * Never <code>null</code>, may be empty.
    */
   private String m_configDefFile = "";
   
   /**
    * The local configuration file that is included in this bundle.
    * Never <code>null</code>, may be empty.
    */
   private String m_localConfigFile = "";
   
   /**
    * There is 1 entry in the list for each package that this package depends
    * upon. Each entry contains 3 properties about the dependent package, its
    * name, version and whether the dependency was determined by the system
    * (implied) or the implementer. Map keys are: {@link #XML_PKG_DEP_NAME}
    * {@link #XML_PKG_DEP_VERSION} {@link #XML_PKG_DEP_IMPLIED}.
    * <p>
    * May be empty, never <code>null</code>.
    */
   private List<Map<String, String>> m_pkgDepList = 
      new ArrayList<Map<String, String>>();
   
   /**
    * The default descriptor version value.
    */
   public static final String DEFAULT_VERSION = "1.0.0";
   
   /**
    * The default descriptor minimum required CMS version.
    */
   public static final String DEFAULT_CMS_MIN_VERSION = "6.6.0";
  
   // private Xml constants
   private static final String XML_ATTR_ID = "id";
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_URL = "url";
   private static final String XML_ATTR_MIN = "min";
   private static final String XML_ATTR_MAX = "max";
   private static final String XML_EL_DESC = "Description";
   private static final String XML_EL_PUBLISHER = "Publisher";
   private static final String XML_EL_CMS_VERSION = "CmsVersion";
   private static final String XML_EL_VERSION = "Version";
   private static final String XML_EL_IMPL_CONFIG_FILE = "ImplConfigFile";
   private static final String XML_EL_LOCAL_CONFIG_FILE = "LocalConfigFile";
   private static final String XML_PKG_DEP_ROOT_NAME = "PKGDependencies";
   private static final String XML_PKG_DEP_EL_NAME = "PKGDependency";
   public static final String XML_PKG_DEP_NAME = "name";
   public static final String XML_PKG_DEP_VERSION = "pkgVersion";   
   public static final String XML_PKG_DEP_IMPLIED = "PKGDepImplied";
}
