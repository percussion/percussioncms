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
package com.percussion.server;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * This class defines the metadata for system properties.  A system property
 * having a meta data entry is a candidate for persistence.
 *
 * Instances of this class are immutable after construction, so they are
 * thread-safe.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSPersistentPropertyMeta")
@Table(name = "PSX_PERSISTEDPROPERTYMETA")
public class PSPersistentPropertyMeta
{
   /**
    * Default constructor, which is needed by hibernate.
    */
   private PSPersistentPropertyMeta()
   {
   }
   
   /**
    *  Represents property's meta data.
    *  @param userName user name associated with the property/session.
    *  @param propertyName name of the property.Cannot be
    *  <code>null</code>.
    *  @param category type of the property.Cannot be
    *  <code>null</code>.
    *  @param propertySaveType specifies values 0,1,2 0r 3.
    *  <ui>
    *  <li>0 - default
    *  <li>1 - persist always.
    *  <li>2 - persist the first time only.
    *  <li>3 - never persist.
    *  @param  overridable specifies whether default attributes can be
    *  overriden by the user.
    *  @param  enabled specifies if persistence is enabled or not.
    *  @param  className specifies the class name used to
    *  serialize/deserialize the value object.
    */
   public PSPersistentPropertyMeta(String userName, String propertyName,
    String category, int propertySaveType,boolean overridable, boolean enabled,
     String className)
   {

      setUserName(userName);
      setCategory(category);

      this.propertyName = propertyName;
      this.propertySaveType = propertySaveType;
      this.overridable = overridable ? 1 : 0;
      enabledState = enabled ? 1 : 0;
   }

   /**
    * @param elem Cannot be <code>null</code>.
    */
   public PSPersistentPropertyMeta(Element elem)
   {
      fromXML(elem);
   }

   /**
    * Copy constructor
    * @param metaObj  Cannot be <code>null</code>.
    * @param userName Username for the new object.  If <code>null</code> is
    * passed in, the value will be taken from the original object.
    */
   public PSPersistentPropertyMeta(PSPersistentPropertyMeta metaObj,
                                   String userName)
   {
      this((userName == null ? metaObj.getUserName() : userName),
           metaObj.getPropertyName(),
           metaObj.getCategory(), metaObj.getPropertySaveType(),
           metaObj.isOverridable(), metaObj.isEnabled(), metaObj.getClassName()
      );
   }

   /**
    * Sets the user name.  The name cannot be "**psxsystem".
    * @param userName The user name; must not be <code>null</code>
    */
   private void setUserName(String userName)
   {
      if (userName == null)
         throw new IllegalArgumentException("User name cannot be null");

      this.userName = userName;
   }

   /**
    * Sets the category.
    * @param category The category; if <code>null</code>, an empty string is
    * used.
    */
   private void setCategory(String category)
   {
      if (category == null)
         category = "";

      this.category = category;
   }


   /**
    * @return user name.Cannot be <code>null</code>.
    */
   public String getUserName()
   {
      return userName;
   }

   /**
    * @return property name.Cannot be <code>null</code>.
    */
   public String getPropertyName()
   {
      return propertyName;
   }

   /**
    * Gets the category type which is an arbitrary string used to group related
    * properties together. All categories beginning with sys_ are reserved by
    * the system. The category for session variables is sys_session.
    * @return category type.  Never <code>null</code>, may be an empty string.
    */
   public String getCategory()
   {
      return category;
   }

   /**
    * Specifies if persistence is enabled or not.
    * @return <code>true</code>, if enabled otherwise <code>false</code>.
    */
   public boolean isEnabled()
   {
      return enabledState == 1;
   }

   /**
    * Specifies if default attributes are overridable or not.
    * @return <code>true</code>,if overridable otherwise <code>false</code>.
    */
   public boolean isOverridable()
   {
      return overridable == 1;
   }

   /**
    * Gets the frequency of persistence.See the constructor doc for details.
    * @return property save type.
    */
   public int getPropertySaveType()
   {
      return propertySaveType;
   }

   /**
    * Gets the class name used for serializing/deserializing values.
    * @return class name.Can be <code>null</code>
    */
   public String getClassName()
   {
      return className;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return propertyId + " " + userName + " " + propertyName + " " +
              category + " " + propertySaveType + " ";
   }

   public int getPropertyId() {
      return propertyId;
   }

   public void setPropertyId(int m_propertyId) {
      this.propertyId = m_propertyId;
   }

   public void setPropertyName(String m_propertyName) {
      this.propertyName = m_propertyName;
   }

   public void setPropertySaveType(int m_propertySaveType) {
      this.propertySaveType = m_propertySaveType;
   }

   public int getEnabledState() {
      return enabledState;
   }

   public void setEnabledState(int m_enabledState) {
      this.enabledState = m_enabledState;
   }

   public int getOverridable() {
      return overridable;
   }

   public void setOverridable(int m_overridable) {
      this.overridable = m_overridable;
   }

   public void setClassName(String m_className) {
      this.className = m_className;
   }

   /**
    * @param elem Cannot be <code>null</code>.
    */
   private void fromXML(Element elem)
   {
      PSXmlTreeWalker lpsxml = new PSXmlTreeWalker(elem);
      int iOverridable =
        Integer.parseInt(lpsxml.getElementData(OVERRIDE_ELEM));
      int iEnabled =
        Integer.parseInt(lpsxml.getElementData(ENABLE_ELEM));
      overridable = iOverridable == 0 ? 0 : 1;
      enabledState = iEnabled == 0 ? 0 : 1;
      userName = lpsxml.getElementData(USERNAME_ELEM);
      propertyName = lpsxml.getElementData(PROPERTYNAME_ELEM);
      setCategory(lpsxml.getElementData(CATEGORY_ELEM));
      propertySaveType =
        Integer.parseInt(lpsxml.getElementData(SAVETYPE_ELEM));
      className = lpsxml.getElementData(CLASSNAME_ELEM);
   }

   /**
    * @param doc The XML document in which to store the object
    * @return XML representation of the object.
    */
   public Element toXML(Document doc)
   {
      Element   root = doc.createElement(ROOT_ELEM);
      PSXmlDocumentBuilder.addElement(doc, root, CATEGORY_ELEM, category);
      PSXmlDocumentBuilder.addElement(doc, root, CLASSNAME_ELEM, className);
      String tmp = "0";
      tmp = String.valueOf(enabledState);
      PSXmlDocumentBuilder.addElement(doc, root, ENABLE_ELEM, tmp);
      tmp = String.valueOf(overridable);
      PSXmlDocumentBuilder.addElement(doc, root, OVERRIDE_ELEM, tmp);
      tmp = Integer.toString(propertyId);
      PSXmlDocumentBuilder.addElement(doc, root, ID_ELEM, tmp);
      PSXmlDocumentBuilder.addElement(doc, root, PROPERTYNAME_ELEM,
              propertyName);
      tmp = Integer.toString(propertySaveType);
      PSXmlDocumentBuilder.addElement(doc, root, SAVETYPE_ELEM, tmp);
      PSXmlDocumentBuilder.addElement(doc, root, USERNAME_ELEM, userName);
      return root;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.userName + "," + this.propertyName);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PSPersistentPropertyMeta other = (PSPersistentPropertyMeta) obj;
      return Objects.equals(this.userName +","+ this.propertyName, other.userName +","+ other.propertyName);
   }
   
   /**
    * Primary key.
    */
   @Id
   @Column(name = "PROPERTYID", nullable = false)
   private int propertyId;

   /**
    * The fully qualified name of the principal associated w/ the property.
    */
   @Basic
   @Column(name = "USERNAME")   
   private String userName = "";

   /**
    * The case-sensitive name of the property to be persisted or overridden.
    */
   @Basic
   @Column(name = "PROPERTYNAME")   
   private String propertyName = "";

   /**
    * An arbitrary string used to group related properties together.
    * All categories beginning with sys_ are reserved by the system.
    */
   @Basic
   @Column(name = "CATEGORY")   
   private String category = "";

   /**
    * A value corresponding to default (0), always (1), first time (2)
    * or never (3).
    */
   @Basic
   @Column(name = "PROPERTYSAVETYPE")   
   private int propertySaveType;

   /**
    * If it is <code>1</code>, then the property is persisted 
    * (based on the PROPERTYSAVETYPE) and restored; otherwise, the property is 
    * not persisted or restored. Defaults to <code>1</code>.
    */
   @Basic
   @Column(name = "ENABLEDSTATE")   
   private int enabledState = 1;

   /**
    * A flag to indicate whether the default attributes can be
    * overridden by user specific attributes.  It is <code>1</code> if 
    * overridden is enabled. Defaults to <code>1</code>.
    */
   @Basic
   @Column(name = "OVERRIDABLE")   
   private int overridable = 1;

   /**
    * The fully qualified name of the extension used to serialize/deserialize
    * the value object of this property if it is not a String.
    *
    * @todo Remove this, or implement a use for it.  It doesn't seem to
    * get used or set anywhere.  (dbreslau)
    */
   @Basic
   @Column(name = "CLASSNAME")   
   private String className = null;

   /**
    * Default entries are against this user name which are later merged
    * with user specified values for the corresponding user.
    */

   public static final String ROOT_ELEM = "PERSISTEDPROPERTYMETA";
   public static final String CATEGORY_ELEM = "CATEGORY";
   public static final String USERNAME_ELEM = "USERNAME";
   public static final String PROPERTYNAME_ELEM = "PROPERTYNAME";
   public static final String SAVETYPE_ELEM = "PROPERTYSAVETYPE";
   public static final String OVERRIDE_ELEM = "OVERRIDABLE";
   public static final String ENABLE_ELEM = "ENABLEDSTATE";
   public static final String CLASSNAME_ELEM = "CLASSNAME";
   public static final String ID_ELEM = "PROPERTYID";
}
