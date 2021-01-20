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
package com.percussion.cms.objectstore;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;

import static org.apache.commons.lang.Validate.notNull;


/**
 * This class contains all the persistent information for a folder object.
 * However, it does not contain the folder relationship information, which is
 * the information about its related child items or folders.
 */
public class PSFolder extends PSDbComponent implements java.io.Serializable
{
   private static final Logger logger = LogManager.getLogger(PSFolder.class);

   /**
    * Creates a PSFolder instance that is not persisted in the database.
    *
    * @param name The name of the folder. Never <code>null</code> or empty.
    *
    * @param communityId The identifier of a community that have access to this
    *    folder, or -1 for all community.
    *
    * @param permissions permissions on the folder encapsulated by this object
    * for the user accessing the folder, should be non-negative. This is a 
    * transient data, see {@link #getPermissions()} for detail.
    *
    * @param description An optional message that describes the folder.
    *    May be <code>null</code> or empty.
    *
    */
   public PSFolder(String name, int communityId, int permissions,
      String description)
   {
      super(new PSLocator());
      init(name, communityId, permissions, description);
   }

   /**
    * Creates an indentical instance from the specified folder object. This does
    * a shallow copy of the key and all data.
    * 
    * @param fromFolder the folder object used to construct the new object, 
    *    must not be <code>null</code>.
    */
   public PSFolder(PSFolder fromFolder)
   {
      super(fromFolder);
      
      m_communityId = fromFolder.m_communityId;
      m_description = fromFolder.m_description;
      m_name = fromFolder.m_name;
      m_properties = fromFolder.m_properties;
      m_acl = fromFolder.m_acl;
      m_locale = fromFolder.m_locale;
      
      // handles transient data
      m_permissions = fromFolder.m_permissions;
      m_folderPath = fromFolder.m_folderPath;
      m_communityName = fromFolder.m_communityName;
      m_displayFormatName = fromFolder.m_displayFormatName;
   }
   
   /**
    * Creates a PSFolder instance that has persisted in the database.
    *
    * @param name The name of the folder. Never <code>null</code> or empty.
    *
    * @param id The identifier of the folder. It may not be less or equal to
    *    <code>0</code>.
    *
    * @param communityId The identifier of a community that have access to this
    *    folder, or -1 for all community.
    *
    * @param permissions permissions on the folder encapsulated by this object
    * for the user accessing the folder, should be non-negative. This is a 
    * transient data, see {@link #getPermissions()} for detail.
    *
    * @param description An optional message that describes the folder.
    *    May be <code>null</code> or empty.
    *
    */
   public PSFolder(String name, int id, int communityId, int permissions,
      String description)
   {
      super(new PSLocator(id, 1));

      init(name, communityId, permissions, description);

      if (id <= 0)
         throw new IllegalArgumentException("id may not be <= 0");
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSDbComponent#setPersisted()
    */
   public void setPersisted() throws PSCmsException
   {
      super.setPersisted();
      m_properties.setPersisted();
      m_acl.setPersisted();
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSDbComponent#getLocator()
    */
   public PSLocator getLocator()
   {
      return (PSLocator) super.getLocator();
   }
   
   /**
    * Gets the publishing filename for the folder. If the folder property named
    * {@link #PROPERTY_PUB_FILE_NAME} is present, not <code>null</code> or 
    * empty, its value will be used as the file name for this folder. If this 
    * property is not defined, the folder name is returned.
    * 
    * @return the publishing file name, never <code>null</code> or empty.
    */
   public String getPubFileName()
   {
      PSFolderProperty prop = getProperty(PROPERTY_PUB_FILE_NAME);
      if (prop != null)
      {
         String pubFileName = prop.getValue().trim();
         if (pubFileName.length() != 0)
            return pubFileName;
         else
            return getName();
      }
      else
      {
         return getName();
      }
   }
   
   /**
    * Utility method to initialize the object with the given parameters.
    *
    * @param name The name of the folder. Never <code>null</code> or empty.
    *
    * @param communityId The identifier of a community that have access to this
    *    folder, or -1 for all community.
    *
    * @param permissions permissions on the folder encapsulated by this object
    * for the user accessing the folder, should be non-negative
    *
    * @param description An optional message that describes the folder.
    *    May be <code>null</code> or empty.
    */
   private void init(String name, int communityId, int permissions,
      String description)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_name = name;

      if (description != null)
         m_description = description;
      else
         m_description = "";

      m_communityId = communityId;

      m_permissions = new PSFolderPermissions(permissions);

      m_properties = new PSDbComponentList(PSFolderProperty.class, null, false);

      m_acl = new PSObjectAcl();
   }

   /**
    * See {@link PSDbComponent#getLookupName() base class} for a description
    */
   protected String getLookupName()
   {
      return "CONTENT";
   }

   /**
    * Creates an instance from a previously serialized (using <code>toXml
    * </code>) folder object.
    *
    * @param source A valid element that meets the dtd defined in the
    *    description of {@link #toXml(Document)}. Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSFolder(Element source) throws PSUnknownNodeTypeException
   {
      super(new PSLocator());
      fromXml(source);
   }

   /**
    * Creates a key for the object.
    *
    * @param values The values of the to be created key. It may be
    *    <code>null</code>. If it is not <code>null</code>, it must contain
    *    2 elemenent. The 1st element is the content id, the 2nd element is
    *    the revision number.
    *
    * @return The created key. It is an empty key if the <code>values</code> is
    *    <code>null</code>, otherwise the key is created with the
    *    <code>values</code>.
    */
   public static PSKey createKey(String[] values)
   {
      if (values != null)
      {
         return new PSLocator();
      }
      else if (values == null || values.length != 2)
      {
         throw new IllegalArgumentException(
            "The values must have 2 elements, 1st element is content id, " +
            "2nd element is the revision number");
      }
      else
      {
         return new PSLocator(values[0], values[1]);
      }
   }

   /**
    * Get the name of the object. The folder name is only unique within amount
    * its siblings.
    *
    * @return The name supplied in the ctor.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * A short message that describes what this folder is for.
    *
    * @return The description, may be empty, but never <code>null</code>.
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Get the community id that has access to this folder object.
    *
    * @return The community id. If it is <code>-1</code>, then this folder
    *    is accessable by all communities.
    */
   public int getCommunityId()
   {
      return m_communityId;
   }

   /**
    * Get locale string of the folder.
    * @return Locale or language string in the standard syntax, e.q. en-us. 
    * Never <code>null</code> or empty.
    */
   public String getLocale()
   {
      return m_locale;
   }

   /**
    * Set the community id.
    *
    * @param communityId The to be set community id.
    */
   public void setCommunityId(int communityId)
   {
      m_communityId = communityId;
      setDirty();
   }

   /**
    * Set a description for the folder object.
    *
    * @param desc The to be set description, may be <code>null</code> or empty.
    */
   public void setDescription(String desc)
   {
      m_description = (desc == null) ? "" : desc;
      setDirty();
   }

   /**
    * Set method for locale of the folder.
    * @param locale Language string representing the locale for the folder. 
    * Must not be <code>null</code> or empty and must be one of the 
    * registered locales in the system. 
    */
   public void setLocale(String locale)
   {
      if(locale==null || locale.length()<0)
      {
         throw new IllegalArgumentException(
         "locale for the folder cannot be null or empty");
      }
      m_locale = locale;
   }

   /**
    * Set a name for the folder object.
    *
    * @param name The to be set name, may not be <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_name = name;
      setDirty();
   }

   /**
    * Checks if the specified folder is a root folder or not. Root folders have
    * predefined IDs.
    * {@link #ROOT_ID ROOT_ID}
    * {@link #SYS_SITES_ID SYS_SITES_ID}
    * {@link #SYS_FOLDERS_ID SYS_FOLDERS_ID}
    *
    * @param id the content id of the folder to check
    *
    * @return <code>true</code> if the specified <code>id</code> matches one of
    * the following:
    * <code>ROOT_ID</code>
    * <code>SYS_SITES_ID</code>
    * <code>SYS_FOLDERS_ID</code>
    *
    * Returns <code>false</code> otherwise.
    */
   public static boolean isRootFolder(int id)
   {
      if ((id == ROOT_ID) || (id == SYS_SITES_ID) || (id == SYS_FOLDERS_ID))
         return true;
      return false;
   }

   /**
    * Checks if this folder is a root folder or not.
    *
    * @return <code>true</code> if this folder is a root folder,
    * <code>false</code> otherwise.
    *
    * See {@link #isRootFolder(int) isRootFolder(int)} for details.
    */
   public boolean isRootFolder()
   {
      PSLocator locator = (PSLocator)getLocator();
      int id = locator.getId();
      return PSFolder.isRootFolder(id);
   }
   
   /**
    * Is this folder marked to be published only for special editions?
    * 
    * @return <code>true</code> if the value of the 
    *    {@link IPSConstants#SYS_PUBLISH_FOLDER_WITH_SITE} property is 
    *    {@link Boolean#TRUE}; <code>false</code> otherwise.
    */
   public boolean isPublishOnlyInSpecialEdition()
   {
      PSFolderProperty property = getProperty(
         IPSConstants.SYS_PUBLISH_FOLDER_WITH_SITE);
      if (property != null)
         return property.getValue().equalsIgnoreCase(Boolean.TRUE.toString());
      
      return false;
   }

   /**
    * Set the {@link IPSConstants#SYS_PUBLISH_FOLDER_WITH_SITE} property 
    * according to the supplied property value. 
    * 
    * @param isFolderPublishProperty <code>true</code> if set the value of
    *    the property to {@link Boolean#TRUE}; otherwise, delete
    *    the property.
    */
   public void setPublishOnlyInSpecialEdition(boolean isFolderPublishProperty)
   {
      if (! isFolderPublishProperty)
         deleteProperty(IPSConstants.SYS_PUBLISH_FOLDER_WITH_SITE);
      else
         setProperty(IPSConstants.SYS_PUBLISH_FOLDER_WITH_SITE, Boolean.TRUE
               .toString());

   }
   
   /**
    * Checks if the specified property is display format property.
    *
    * @return <code>true</code> if the specified property is display format
    * property, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>property</code> is
    * <code>null</code>
    */
   public static boolean isDisplayFormatProperty(PSFolderProperty property)
   {
      if (property == null)
         throw new IllegalArgumentException("property may not be null");

      return (property.getName().equalsIgnoreCase(PROPERTY_DISPLAYFORMATID));
   }
   
   /**
    * Checks if the specified property is the site folder publishing property.
    *
    * @param property the folder property, never <code>null</code>.
    * 
    * @return <code>true</code> if the specified property is folder
    * publishing property, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>property</code> is
    * <code>null</code>
    */
   public static boolean isFolderPublishProperty(PSFolderProperty property)
   {
      if (property == null)
         throw new IllegalArgumentException("property may not be null");

      return (property.getName().equalsIgnoreCase(
         IPSConstants.SYS_PUBLISH_FOLDER_WITH_SITE));
   }

   /**
    * Get the value of display format property, which is the id of the display
    * format, not the name of it.
    *
    * @return <code>null</code> if the display format property does not exist,
    * otherwise return the value (id) of the display format property,
    * may be empty.
    */
   public String getDisplayFormatPropertyValue()
   {
      return getPropertyValue(PROPERTY_DISPLAYFORMATID);
   }
   
   /**
    * Gets the display format id property.
    * 
    * @return the id of the display format. It may be <code>-1</code> if the 
    *    property does not exist.
    */
   public int getDisplayFormatId()
   {
      String idString = getDisplayFormatPropertyValue();
      if (StringUtils.isBlank(idString))
         return -1;
      else
         return Integer.parseInt(idString);
   }

   /**
    * Sets the display format property value.
    *
    * @param value the value of the display format property, which must be
    *    the id of the display format, not the name of it. It may not be
    *    <code>null</code>
    *
    * @throws IllegalArgumentException if <code>value</code> is
    * <code>null</code>
    */
   public void setDisplayFormatPropertyValue(String value)
   {
      setProperty(PROPERTY_DISPLAYFORMATID, value);
   }

   
   /**
    * Sets the display format id property.
    * 
    * @param displayFormatId the new display format id.
    */
   public void setDisplayFormatId(int displayFormatId)
   {
      setDisplayFormatPropertyValue(String.valueOf(displayFormatId));
   }
   
   /**
    * Get the global template property.
    * 
    * @return the global template property name. It may be <code>null</code> or
    *    empty if the global template is inherited from its parent.
    */
   public String getGlobalTemplateProperty()
   {
      return getPropertyValue(PROPERTY_GLOBALTEMPLATE);
   }

   /**
    * Set the global template property.
    * 
    * @param value the name of the global template. It may be <code>null</code>
    *    or empty if the global template is inherited from its parent and the
    *    original global template property will be deleted.
    */
   public void setGlobalTemplateProperty(String value)
   {
      if (value == null || value.trim().length() == 0)
         deleteProperty(PROPERTY_GLOBALTEMPLATE);
      else
         setProperty(PROPERTY_GLOBALTEMPLATE, value.trim());
   }
   
   /**
    * Checks if the specified property is the global template property.
    *
    * @param property the property to test, not <code>null</code>.
    * @return <code>true</code> if the specified property is the folder
    *    global template property, <code>false</code> otherwise.
    */
   public static boolean isFolderGlobalTemplateProperty(
      PSFolderProperty property)
   {
      if (property == null)
         throw new IllegalArgumentException("property may not be null");

      return property.getName().equalsIgnoreCase(PROPERTY_GLOBALTEMPLATE);
   }

   /**
    * Returns the ACL set on this folder. This ACL contains zero or more ACL
    * entries. Each ACL entry specifies the level of permission for a
    * particular user, role or virtual entry (Folder Community, Everyone).
    *
    * @return the ACL set on this folder, never <code>null</code>.
    */
   public PSObjectAcl getAcl()
   {
      return m_acl;
   }

   /**
    * Sets the ACL set on this folder.
    *
    * @param acl the ACL to set on the folder encapsulated by this object,
    * may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>acl</code> is <code>null</code>
    *
    * @see PSFolder#getAcl() getAcl
    */
   public void setAcl(PSObjectAcl acl)
   {
      if (acl == null)
         throw new IllegalArgumentException("acl may not be null");
      m_acl = acl;
   }

   /**
    * Returns a <code>PSObjectPermissions</code> object which encapsulates an
    * the permissions on this folder for the user accessing the folder.
    * <p>
    * This is a transient data, and it will not be persisted with the folder 
    * object when the folder is saved into the repository.
    *
    * @return the permissions set on the folder encapsulated by this object,
    * never <code>null</code>
    */
   public PSObjectPermissions getPermissions()
   {
      return m_permissions;
   }

   /**
    * Sets the specified folder permissions for this object.
    * <p>
    * This is a transient data, and it will not be persisted with the folder 
    * object when the folder is saved into the repository.
    * 
    * @param permissions the new folder permissions, never <code>null</code>.
    */
   public void setPermissions(PSFolderPermissions permissions)
   {
      if (permissions == null)
         throw new IllegalArgumentException("permissions may not be null.");
      
      m_permissions = permissions;
   }
   
   /**
    * This method has been overridden to also consider the equivalence of
    * the folder's locator as we consider the locator a necessary part
    * of the definition of a folders equivalence. We would not normally consider
    * the locater when determining object equivalence, but folder is a special
    * case.
    * @param o the object to check for equivalence against.
    * @return <code>true</code> if equivalent. 
    */
   public boolean equals( Object o )
   {
      if ( !(o instanceof PSFolder ))
         return false;

      PSFolder other = (PSFolder) o;
      
      int id1 = getKeyPartInt("CONTENTID", -1);
      int id2 = other.getKeyPartInt("CONTENTID", -1);
               
      EqualsBuilder compare = new EqualsBuilder()
         .append(id1, id2)
         .append(m_name, other.m_name)
         .append(m_communityId, other.m_communityId)
         .append(m_description, other.m_description)
         .append(m_properties, other.m_properties)
         .append(m_acl, other.m_acl)
         .append(m_locale, other.m_locale)
         .append(m_folderPath, other.m_folderPath)       // transient
         .append(m_communityName, other.m_communityName) // transient
         .append(m_permissions, other.m_permissions)     // transient
         .append(m_displayFormatName, other.m_displayFormatName); // transient

      return compare.isEquals();
   }

   /**
    * See {@link IPSDbComponent#equalsFull(Object)}
    */
   public boolean equalsFull( Object o )
   {
      if (equals(o))
      {
         return super.equalsFull(o);
      }
      else
      {
         return false;
      }
   }

   /**
    * This method has been overridden to also consider the folder's locator as
    * part of the hashcode, as we consider the locator a necessary part of the
    * folder's identity.
    * 
    * @return the calculated hashcode
    */
   public int hashCode()
   {
      return getKeyPartInt("CONTENTID", 0) 
         + m_name.hashCode() 
         + m_description.hashCode() 
         + m_communityId + m_properties.hashCode();
   }

   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following dtd:
    * <pre>
    * &lt;!ELEMENT PSFolder (PSXLocator, Description, PSXComponentList?)>
    * &lt;!ATTLIST PSFolder
    *    name         CDATA #REQUIRED
    *    communityId  CDATA #REQUIRED
    *    >
    * &lt;!ELEMENT Description (#PCDATA)>
    * </pre>
    *
    * @param doc Used to generate the element. Never <code>null</code>.
    *
    * @return the generated element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("doc must be supplied");

      Element root = super.toXml(doc);

      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_COMMUNITYID, Integer.toString(m_communityId));
      root.setAttribute(XML_ATTR_PERMISSIONS,
         "" + m_permissions.getPermissions());

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_DESCRIPTION,
         m_description);

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_LOCALE,
         m_locale);

      if ( m_properties.size() > 0 )
         root.appendChild(m_properties.toXml(doc));

      root.appendChild(m_acl.toXml(doc));

      return root;
   }

   public String toString()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element elem = toXml(doc);
      return PSXmlDocumentBuilder.toString(elem);
   }
   
   /**
    * See {@link IPSDbComponent#fromXml(Element)}
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      super.fromXml(sourceNode); // set data for super class

      PSXMLDomUtil.checkNode(sourceNode, getNodeName());

      m_name = PSXMLDomUtil.checkAttribute(sourceNode, XML_ATTR_NAME, true);
      m_communityId = PSXMLDomUtil.checkAttributeInt(sourceNode,
         XML_ATTR_COMMUNITYID, true);
      int permissions = PSXMLDomUtil.checkAttributeInt(sourceNode,
         XML_ATTR_PERMISSIONS, true);
      m_permissions = new PSFolderPermissions(permissions);

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);

      // folder description
      Element el = walker.getNextElement(XML_NODE_DESCRIPTION, firstFlags);
      if (el != null)
         m_description = PSXmlTreeWalker.getElementData(el);

      // folder locale
      el = walker.getNextElement(XML_NODE_LOCALE, nextFlags);
      if (el != null)
         m_locale = PSXmlTreeWalker.getElementData(el);

      // folder properties
      m_properties = new PSDbComponentList(PSFolderProperty.class, null, false);
      walker.setCurrent(sourceNode);
      el = walker.getNextElement("PSXDbComponentList", firstFlags);
      if (el != null)
         m_properties.fromXml(el);

      // folder ACL
      m_acl = new PSObjectAcl();
      walker.setCurrent(sourceNode);
      el = walker.getNextElement(PSObjectAcl.XML_NODE_NAME, firstFlags);
      if (el != null)
         m_acl.fromXml(el);
   }

   /**
    * Copy data from a specified object to this object, performing a deep copy
    * of all members, except the key.
    *
    * @param obj The object from which to copy values. It has to be the same
    *    type as the current object. It may not be <code>null</code>.
    */
   public void copyFrom(IPSDbComponent obj)
   {
      if ( !(obj instanceof PSFolder ))
         throw new IllegalArgumentException(
            "obj must be an instance of PSFolder");

      PSFolder from = (PSFolder) obj;
      if (! equals(from))
      {
         m_name = from.m_name;
         m_description = from.m_description;
         m_communityId = from.m_communityId;
         m_locale = from.m_locale;
         m_properties = (PSDbComponentList) from.m_properties.clone();
         m_acl = (PSObjectAcl) from.m_acl.clone();
         
         // transient data
         m_permissions = (PSFolderPermissions)from.m_permissions.clone();
         m_displayFormatName = from.m_displayFormatName;
         m_folderPath = from.m_folderPath;
      }
   }
   
   /**
    * Copies and merges data from the specified folder.
    * 
    * @param from the to be merged folder, never <code>null</code>. 
    */
   public void mergeFrom(PSFolder from)
   {
      if (from == null)
         throw new IllegalArgumentException("from may not be null.");

      // handle non-transient data
      m_name = from.m_name;
      m_description = from.m_description;
      m_communityId = from.m_communityId;
      m_locale = from.m_locale;
      mergePropertiesFrom(from.m_properties);
      mergeAclFrom(from.m_acl);
      
      // handle transient data
      m_permissions = (PSFolderPermissions)from.m_permissions.clone();
      m_folderPath = from.m_folderPath;
      m_communityName = from.m_communityName;
      m_displayFormatName = from.m_displayFormatName;
   }
   
   
   /**
    * Merges current ACL with the specified source acl.  New entries
    * will be added, deleted entries will be removed, and updated
    * entries will be modified.
    * 
    * @param srcAcl the to be merged ACL, may not be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void mergeAclFrom(PSObjectAcl srcAcl)
   {
      notNull(srcAcl);
         
      PSObjectAcl origAcl = (PSObjectAcl) m_acl.cloneFull();
      PSObjectAcl srcAclClone = (PSObjectAcl) srcAcl.cloneFull();
      PSObjectAclEntry entry, srcEntry, tgtEntry;
      
      // Base on the current ACL, handles the modified and deleted ACLs
      Iterator entries = origAcl.iterator();
      while (entries.hasNext())
      {
         entry = (PSObjectAclEntry) entries.next();
         tgtEntry = m_acl.getAclEntry(entry.getName(), entry.getType());
         
         srcEntry = srcAclClone.getAclEntry(entry.getName(), entry.getType());
         if (srcEntry != null)
         {
            tgtEntry.setPermissions(srcEntry.getPermissions());
            srcAclClone.remove(srcEntry);
         }
         else
            m_acl.remove(tgtEntry);
      }
      
      // Base on the source ACLs, handles the new ACLs
      entries = srcAclClone.iterator();
      while (entries.hasNext())
      {
         srcEntry = (PSObjectAclEntry) entries.next();
         if (srcEntry.isPersisted())
            throw new IllegalStateException("The new ACL entry, ("
                  + srcEntry.toString()
                  + ") , from the source ACL must not be persisted.");
         
         m_acl.add(srcEntry);
      }      
   }


   /**
    * Merges the properties with the specified properties. 
    *  
    * @param srcProps the to be merged properties, assumed not <code>null</code>.
    */
   private void mergePropertiesFrom(PSDbComponentList srcProps)
   {
      PSFolderProperty prop, srcProp, tgtProp;
      
      // handles the modified and deleted properties, based on current object.
      PSDbComponentList origProps = (PSDbComponentList) m_properties.cloneFull();
      Iterator props = origProps.iterator();
      while (props.hasNext())
      {
         prop = (PSFolderProperty) props.next();
         String pname = prop.getName();
         srcProp = getProperty(pname, srcProps);
         if (srcProp != null)
         {
            // update the existing property
            tgtProp = getProperty(pname);
            tgtProp.setDescription(srcProp.getDescription());
            tgtProp.setValue(srcProp.getValue());
         }
         else
         {
            // not in the source properties, then remove it
            m_properties.remove(getProperty(pname));
         }
      }
      
      // walk through the source (or new) properties, handles inserted
      // (or new) properties.
      props = srcProps.iterator();
      while (props.hasNext())
      {
         prop = (PSFolderProperty) props.next();
         if (getProperty(prop.getName()) == null)
         {
            if (prop.isPersisted())
               throw new IllegalStateException("The new property, name="
                     + prop.getName()
                     + ", from the source properties must not be persisted.");

            addProperty(prop);
         }
      }
   }
   
   /**
    * A cloned folder does not copy the locator and will therefore
    * not be considered equal to the original folder.
    * 
    * @see IPSDbComponent#clone() for more details
    */
   @Override
   public Object clone()
   {
      PSFolder copy = (PSFolder) super.clone();
      copy.m_communityId = m_communityId;
      copy.m_description = m_description;
      copy.m_name = m_name;
      copy.m_properties = (PSDbComponentList) m_properties.clone();
      copy.m_acl = (PSObjectAcl)m_acl.clone();
      copy.m_locale = m_locale;
      
      // handles transient data
      copy.m_permissions = (PSFolderPermissions)m_permissions.clone();
      copy.m_folderPath = m_folderPath;
      copy.m_communityName = m_communityName;
      copy.m_displayFormatName = m_displayFormatName;

      return copy;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSDbComponent#cloneFull()
    */
   @Override
   public Object cloneFull()
   {
      PSFolder copy = (PSFolder) super.cloneFull();
      copy.m_communityId = m_communityId;
      copy.m_description = m_description;
      copy.m_name = m_name;
      copy.m_properties = (PSDbComponentList) m_properties.cloneFull();
      copy.m_acl = (PSObjectAcl)m_acl.cloneFull();
      copy.m_locale = m_locale;

      // handles transient data
      copy.m_permissions = (PSFolderPermissions) m_permissions.clone();
      copy.m_folderPath = m_folderPath;
      copy.m_communityName = m_communityName;
      copy.m_displayFormatName = m_displayFormatName;

      return copy;
   }

   /**
    * See {@link PSDbComponent#getState()}
    */
   public int getState()
   {
      if ( super.getState() != IPSDbComponent.DBSTATE_UNMODIFIED)
      {
         return super.getState();
      }
      else if ( m_properties.getState() == IPSDbComponent.DBSTATE_UNMODIFIED)
      {
         return IPSDbComponent.DBSTATE_UNMODIFIED;
      }
      else
      {
         return IPSDbComponent.DBSTATE_MODIFIED;
      }
   }

   /**
    * See {@link PSDbComponent#createKey(Element)}
    */
   protected PSKey createKey(Element keyEl)  throws PSUnknownNodeTypeException
   {
      return (PSKey) new PSLocator(keyEl);
   }

   /**
    * Get the list of properties.
    *
    * @return An iterator with zero or more <code>PSFolderProperty</code>
    *    object. Never <code>null</code>, but may be empty.
    */
   public Iterator getProperties()
   {
      return m_properties.iterator();
   }

   /**
    * Get a list of to be deleted properties.
    *
    * @return An iterator with zero or more <code>PSFolderProperty</code>
    *    object. Never <code>null</code>, but may be empty.
    */
   public Iterator getDeletedProperties()
   {
      return m_properties.getDeleteCollection().iterator();
   }

   /**
    * Get the total number of properties in the object.
    *
    * @return Greater or equal to <code>0</code>.
    */
   public int getPropertySize()
   {
      return m_properties.size();
   }


   /**
    * Set a property to the folder instance. If the property already exist
    * in the properties list, it will modify the existing one; otherwise, add
    * this property as a new one into the properties list. It searches
    * properties in case insensitive fashion, see {@link #getProperty(String)}.
    *
    * @param name The name of the property, may not be <code>null</code> or
    *    empty.
    *
    * @param value The value of the property, may not be <code>null</code>
    *    but may be empty.
    *
    * @param desc The description of the property, may be <code>null</code>
    *    or empty.
    */
   public void setProperty(String name, String value, String desc)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      if (value == null)
         throw new IllegalArgumentException("value may not be null");
      if (desc == null)
         desc = "";

      PSFolderProperty prop = getProperty(name);
      if ( prop != null )
      {
         prop.setValue(value);
         prop.setDescription(desc);
      }
      else
      {
         prop = new PSFolderProperty(name, value, desc);
         m_properties.add(prop);
      }
   }

   /**
    * Just like {@link #setProperty(String,String,String) setProperty}, except
    * it set an empty description.
    */
   public void setProperty(String name, String value)
   {
      setProperty(name, value, "");
   }

   /**
    * Set a property to the folder instance. If the property already exist
    * in the properties list, it will modify the existing one, otherwise it will
    * add this property as a new one into the properties list. It searches
    * properties in case insensitive fashion, see {@link #getProperty(String)}.
    *
    * @param property the folder property, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>value</code> is
    * <code>null</code>
    */
   public void setProperty(PSFolderProperty property)
   {
      if (property == null)
         throw new IllegalArgumentException("property may not be null");
      setProperty(property.getName(), property.getValue(),
         property.getDescription());
   }

   /**
    * Adds a property to this object.
    *
    * @param prop The to be added property, may not be <code>null</code> and
    *    it must not be exist.
    */
   public void addProperty(PSFolderProperty prop)
   {
      if (prop == null)
         throw new IllegalArgumentException("prop may not be null");

      if (getProperty(prop.getName()) != null) {
         logger.warn("Warning: property \"" + prop.getName() +
                 "\" must be a new one");
      }else{
         m_properties.add(prop);
      }


   }

   /**
    * Get the value of a given property name.
    *
    * @param name The name of the property, may not be empty or
    *    <code>null</code>.
    *
    * @return <code>null</code> if the property does not exist; otherwise
    *    return the value of the property, it may be empty.
    */
   public String getPropertyValue(String name)
   {
      PSFolderProperty prop = getProperty(name);
      if (prop == null)
         return null;
      else
         return prop.getValue();
   }

   /**
    * Get the description of a given property name.
    *
    * @param name The name of the property, may not be empty or
    *    <code>null</code>.
    *
    * @return <code>null</code> if the property does not exist; otherwise
    *    return the value of the property, it may be empty.
    */
   public String getPropertyDesciption(String name)
   {
      PSFolderProperty prop = getProperty(name);
      if (prop == null)
         return null;
      else
         return prop.getDescription();
   }

   /**
    * Get the property object by name, ignore case consideration.
    *
    * @param name The name of the property, may not be <code>null</code> or
    * empty.
    *
    * @return The property object if exist, <code>null</code> if not exist.
    *
    * @throws IllegalArgumentException if <code>name</code> is
    * <code>null</code> or empty
    */
   public PSFolderProperty getProperty(String name)
   {
      if ((name == null) || (name.trim().length() < 1))
         throw new IllegalArgumentException("name may not be null or empty");
      
      return getProperty(name, m_properties);
   }
   
   /**
    * Gets a specified property from a specified property list.
    * 
    * @param name the specified property name, assumed not <code>null</code> 
    *    or empty.
    * @param props the specified property list, assumed not <code>null</code>.
    * 
    * @return the specified property. It may be <code>null</code> if the 
    *    specified property does not exist in the specified property list.
    */
   private PSFolderProperty getProperty(String name, PSDbComponentList props)
   {
      Iterator entries = props.iterator();
      while (entries.hasNext())
      {
         PSFolderProperty prop = (PSFolderProperty) entries.next();
         if (name.equalsIgnoreCase(prop.getName()))
            return prop;
      }

      return null;

   }

   /**
    * Delete a property if exist.
    *
    * @param name The to be deleted name of the property.
    */
   public void deleteProperty(String name)
   {
      PSFolderProperty prop = getProperty(name);
      if (prop != null)
         m_properties.remove(prop);
   }

   /**
    * Sets the name of the Community. The name of the community is a transient 
    * data, and it will not be persisted with the folder object. 
    * 
    * @param communityName the new name of the Community, may be null or empty. 
    */
   public void setCommunityName(String communityName)
   {
      m_communityName = communityName;
   }
   
   /**
    * Gets the name of the Community. The name of the community is a transient 
    * data, and it will not be persisted with the folder object. Defaults to
    * <code>null</code>.
    * 
    * @return the name of the Community. It may be <code>null</code> or empty
    *    if has not been set by {@link #setCommunityName(String)} or the 
    *    community id is <code>-1</code>.
    */
   public String getCommunityName()
   {
      return (m_communityId == -1) ? null : m_communityName;
   }

   /**
    * Sets the name of the display format. The name of the display format is a 
    * transient data, and it will not be persisted with the folder object. 
    * Defaults to <code>null</code>.
    * 
    * @param displayFormatName the new display format name, never 
    *    <code>null</code> or empty.
    */
   public void setDisplayFormatName(String displayFormatName)
   {
      if (StringUtils.isBlank(displayFormatName))
         throw new IllegalArgumentException(
               "displayFormatName must not be null or empty.");

      m_displayFormatName = displayFormatName;
   }
   
   /**
    * Gets the name of the display format. The name of the display format is a 
    * transient data, and it will not be persisted with the folder object. 
    * Defaults to <code>null</code>.
    * 
    * @return the name of the display format. It may be <code>null</code> or
    *    empty if has not been set by {@link #setDisplayFormatName(String)}.
    */
   public String getDisplayFormatName()
   {
      return m_displayFormatName;
   }
   
   /**
    * Sets the folder path. The folder path is a transient data, and will not
    * be persisted with the folder object. Defaults to <code>null</code>.
    * 
    * @param folderPath the new folder path, may not be <code>null</code> or 
    *    empty.
    */
   public void setFolderPath(String folderPath)
   {
      if (StringUtils.isBlank(folderPath))
         throw new IllegalArgumentException(
               "folderPath must not be null or empty.");
     
      m_folderPath = folderPath;
   }

   /**
    * Gets the folder path. The folder path is a transient data, and will not
    * be persisted with the folder object. Defaults to <code>null</code>.
    * 
    * @return the folder path, may not be <code>null</code> or empty if has
    *    not been set by {@link #setFolderPath(String)}.
    */
   public String getFolderPath()
   {
      return m_folderPath;
   }
   
   /**
    * Gets the unique identifier for this folder.
    * 
    * @return The unique id, may be <code>null</code> if the folder was not
    * loaded via the ws layer.
    */
   public IPSGuid getGuid()
   {
      return m_guid;
   }
   
   /**
    * Sets the unique identifier for this folder.
    * 
    * @param guid The unique id, may not be <code>null</code>
    */
   public void setGuid(IPSGuid guid)
   {
      if (guid == null)
      {
         throw new IllegalArgumentException("guid may not be null");
      }
      
      m_guid = guid;
   }
   
   /**
    * The path of the folder. This is a transient data, and will not be 
    * persisted with the folder object. Defaults to <code>null</code>.
    */
   private String m_folderPath = null;
   
   /**
    * The name of the display format. This is a transient data, and will not be
    * persisted with the folder object. Defaults to <code>null</code>.
    */
   private String m_displayFormatName = null;
   
   /**
    * The name of the folder. Initialized by the constructor.
    * Never <code>null</code> or empty after that.
    */
   private String m_name;

   /**
    * The community id that has access to this folder. <code>-1</code> if
    * this folder can be accessed by all community. Initialized by constructor.
    */
   private int m_communityId;

   /**
    * The name of the community. Defaults to <code>null</code>. This is a 
    * transient data and will not persisted with this object when the folder
    * object is saved into the repository.
    */
   private String m_communityName = null;
   
   /**
    * The description of the folder. Initialized by constructor, never
    * <code>null</code>, but may be empty, after that.
    */
   private String m_description;

   /**
    * The locale of the folder. Set by constructor or set method, never
    * <code>null</code>, or empty.
    */
   private String m_locale = PSI18nUtils.DEFAULT_LANG;

   /**
    * Maps the name of the property (as the map key) to the value of the
    * property (as the map value). Initialized by constructor, never
    * <code>null</code>, but may be empty, after that.
    */
   private PSDbComponentList m_properties = null;

   /**
    * Specifies the permissions set on the folder for the user accessing the
    * folder. Initialized in the <code>init()</code> or <code>fromXml()</code>
    * methods. Never <code>null</code> or modified after initialization.
    * <p>
    * Note, this is a transient object, and it will not be persisted with the
    * folder object.
    */
   private PSFolderPermissions m_permissions = null;

   /**
    * Contains ACL entries. Each ACL entry specifies the level of permission
    * for a particular user, role or virtual entry (Folder Community, Everyone)
    * Initialized in the constructor or <code>fromXml()</code> method,
    * Never <code>null</code> after initialization, modified using
    * <code>setAcl()</code> method.
    */
   private PSObjectAcl m_acl = null;

   /**
    * The global unique identifier for this folder when the folder is created
    * via the ws layer.  May be <code>null</code>.
    */
   private IPSGuid m_guid = null;

   /**
    * Folders are implemented as a content type. This is the type id. This
    * is public for availability to other parts of the server. It should not
    * be used by anyone else and no assumptions about the implementation 
    * should be made as it could change.
    */
   public static final int FOLDER_CONTENT_TYPE_ID = 101;
   
   /**
    * Name of the optional property of a folder that inidcates the file name for
    * publishing. This property is used to override the default name which is
    * folder name used to generate publish location.
    */
   public static final String PROPERTY_PUB_FILE_NAME = "sys_pubFilename";
   
   /**
    * The id of the system folder 'Folders'.
    */
   public static final int SYS_FOLDERS_ID = 3;
   
   /**
    * The separator used for folder path
    */
   public static final String PATH_SEP = "/";

   /**
    * Hidden System folder id.
    */
   public static final int SYS_SYSTEM_FOLDER_ID=4;

   /**
    * Hidden System folder id.
    */
   public static final int SYS_SYSTEM_TEMPLATES_FOLDER_ID=5;

   /**
    * Hidden System folder id.
    */
   public static final int SYS_SYSTEM_USERPROFILES_FOLDER_ID=6;

   /**
    * The id of the system assets folder.
    */
   public static final int SYS_ASSETS_ID = 7;

   /**
    * The id of the system site 'Sites'.
    */
   public static final int SYS_SITES_ID = 2;

   /***
    * Recycing folder id
    */
   public static final int SYS_RECYCLING_ID=8;

   /**
    * The id of the root folder 'Root'.
    */
   public static final int ROOT_ID = 1;

   /**
    * The sys_title the root folder.
    */
   public static final String ROOT_TITLE = "Root";
   
   /**
    * Constant for the display format property
    */
   public static final String PROPERTY_DISPLAYFORMATID = "sys_displayformat";

   /**
    * Constant for the global template property name.
    */
   public static final String PROPERTY_GLOBALTEMPLATE = "sys_globaltemplate";
   
   /**
    * Constant used to save as global template property value if the global
    * template is inherited from the parent.
    */
   public static final String INHERIT_FROM_PARENT = "inherit_from_parent";

   /**
    * Generated serial version id. 
    */
   private static final long serialVersionUID = -1771578151351445810L;

   // Private constants for XML attribute and element name
   private final static String XML_ATTR_NAME = "name";
   private final static String XML_ATTR_COMMUNITYID = "communityId";
   private final static String XML_ATTR_PERMISSIONS = "permissions";
   private final static String XML_NODE_DESCRIPTION = "Description";
   private final static String XML_NODE_LOCALE = "Locale";
}
