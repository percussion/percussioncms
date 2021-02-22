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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;

/**
 * Property to be persisted.  Always has a meta entry corresponding to it.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSPersistentProperty")
@Table(name = "PSX_PERSISTEDPROPERTYVALUES")
@IdClass(PSPersistentPropertyPK.class)
public class PSPersistentProperty
{
// dbreslau 12/11/02: A class with public setters is potentially non-
// thread safe, and there are multiple threads accessing the same
// instance of this class.
//
// Rather than synchronize everything (which wouldn't solve the
// problem anyway), I made all setters have private or package protection,
// matching the way they're currently used.  Current usage from
// PSPersistentPropertyManager is done in a thread-safe manner, so changing
// the protections should help ensure that this stays thread-safe.
//
// Ideally this class should be immutable, but that would take
// a little more work.  As it is, only the value and "action"
// (currently named "extraParam") fields are settable.

   /**
    * Default constructor, which is needed by hibernate
    */
   private PSPersistentProperty()
   {
   }
   
   /**
    * Represents property's meta data.
    * @param userName user name associated with the property/session.
    * @param propertyName  Name name of the property.Cannot be <code>null</code>.
    * @param category type of the property.Can be <code>null</code>.
    * @param context  Can be <code>null</code>.
    * @param propertyValue  Cannot be <code>null</code>.
    */
   public PSPersistentProperty(String userName, String propertyName,
    String category, String context, String propertyValue)
   {
      setUserName(userName);
      setName(propertyName);
      setCategory(category);
      setValue(propertyValue);
      setContext(context);
   }

   /**
    * Pseudo-copy constructor
    * @param propObj Cannot be <code>null</code>.
    * @param userName if non-<code>null</code>, overrides the user name in the
    * <code>propObj</code>
    * @param action  Initial value for the <code>extraParam</code> (aka
    * "action") field
    */
   public PSPersistentProperty(PSPersistentProperty propObj,
                               String userName,
                               String action)
   {
      this(
            (userName == null ? propObj.getUserName() : userName),
            propObj.getName(),
            propObj.getCategory(),
            propObj.getContext(),
            propObj.getValue());

      setExtraParam(action);
   }

   /**
    * @param elem Cannot be <code>null</code>.
    */
   public PSPersistentProperty(Element elem)
   {
      fromXML(elem);
   }

   /**
    * Sets the context value.
    * @param context May be <code>null</code>, in which case an empty
    * string is set.
    */
   private void setContext(String context)
   {
      m_context = (context == null ? "" : context);
   }

   /**
    * Sets the user name.  The name cannot be "**psxsystem". May be
    * <code>null</code>, in which case an empty string is set.
    * @param userName May be <code>null</code>, in which case an empty
    * string is set.
    */
   private void setUserName(String userName)
   {
      m_userName = (userName == null ? "" : userName);
   }

   /**
    * Sets the case-sensitive name of the property to be persisted or
    * overridden.
    * @param propertyName May be <code>null</code>, in which case an empty
    * string is set.
    */
   private void setName(String propertyName)
   {
      m_propertyName = (propertyName == null ? "" : propertyName);
   }

   /**
    * Sets the category type which is an arbitrary string used to group related
    * properties together. All categories beginning with sys_ are reserved by
    * the system. The category for session variables is sys_session.
    *
    * @param category May be <code>null</code>, in which case an empty
    * string is set.
    */
   private void setCategory(String category)
   {
      m_category = (category == null ? "" : category);
   }

   /**
    * Sets the value of this property.
    *
    * @param propertyValue May be <code>null</code>, in which case an empty
    * string is set.
    */
   // dbreslau: synchronized this to avoid toXML() being invoked concurrently
   public void setValue(String propertyValue)
   {
      m_propertyValue = (propertyValue == null ? "" : propertyValue);
   }


   /**
    * Gets the name of the user to whom this property belongs.
    * @return user name. Never <code>null</code>; may be an empty string.
    */
   public String getUserName()
   {
      return m_userName;
   }

   /**
    * Gets the name of this property.
    * @return property name. Never <code>null</code>; may be empty string.
    */
   public String getName()
   {
      return m_propertyName;
   }

   /**
    * Gets the category type, which is an arbitrary string used to group related
    * properties together. All categories beginning with sys_ are reserved by
    * the system. The category for session variables is sys_session.
    * @return category type. Never <code>null</code>; may be an empty string.
    */
   public String getCategory()
   {
      return m_category;
   }

   /**
    * Gets the current property value.
    *
    * @return property value.  Never <code>null</code>; may be an empty string.
    */
   public String getValue()
   {
      return m_propertyValue;
   }

   /**
    * Gets the context to which the property belongs.
    *
    * @return context to which the property belongs.  Never <code>null</code>;
    * may be an empty string.
    */
   public String getContext()
   {
      return m_context;
   }

   /**
    * Sets the action to be performed on the property: delete, update or insert
    *
    * @param action May be <code>null</code>, in which case an empty
    * string is set.
    *
    * @todo rename setExtraParam to setActionType() ?
    */
   // dbreslau: synchronized this to avoid toXML() being invoked concurrently
   public synchronized void setExtraParam(String action)
   {
      m_action = (action == null ? "" : action);
   }

   /**
    * Gets the action type
    *
    * @return Never <code>null</code>; may be an empty string.
    *
    * @todo rename getExtraParam to getActionType() ?
    */
   public synchronized String getExtraParam()
   {
      return m_action;
   }

   /**
    * Constructs the property from an XML Element
    * @param elem Cannot be <code>null</code>.
    */
   private void fromXML(Element elem)
   {
      PSXmlTreeWalker lpsxml = new PSXmlTreeWalker(elem);
      setUserName(lpsxml.getElementData(
                              PSPersistentPropertyMeta.USERNAME_ELEM));
      setName(lpsxml.getElementData(
                               PSPersistentPropertyMeta.PROPERTYNAME_ELEM));
      setCategory(lpsxml.getElementData(
                               PSPersistentPropertyMeta.CATEGORY_ELEM));
      setValue(lpsxml.getElementData(PROPERTYVALUE_ELEM));
      setContext(lpsxml.getElementData(CONTEXT_ELEM));
   }

   /**
    * Persists the property to an element in an XML document
    * @param doc XML Document in which the element is stored; must not be
    * <code>null</code>
    * @return XML representation of the object.
    */
   synchronized Element toXML(Document doc)
   {
      Element   root = doc.createElement(PERSISTEDPROPERTYVALUES_ELEM);
      root.setAttribute(DBACTION, m_action);
      PSXmlDocumentBuilder.addElement(doc, root,
      PSPersistentPropertyMeta.CATEGORY_ELEM, m_category);
      PSXmlDocumentBuilder.addElement(doc, root, CONTEXT_ELEM, m_context);
      PSXmlDocumentBuilder.addElement(doc, root,
            PSPersistentPropertyMeta.PROPERTYNAME_ELEM, m_propertyName);
      PSXmlDocumentBuilder.addElement(doc, root,
          PSPersistentPropertyMeta.USERNAME_ELEM, m_userName);
      PSXmlDocumentBuilder.addElement(doc, root, PROPERTYVALUE_ELEM,
                                      m_propertyValue);
      return root;
   }

   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /**
    * Interrelated usages in which a property might be used e.g whether it's
    * designer or a system property.
    */
   @Id
   @Column(name = "CONTEXT", nullable = false)
   private String m_context = "";

   /**
    * The fully qualified name of the principal associated with the property.
    */
   @Id
   @Column(name = "USERNAME", nullable = false)
   private String m_userName  = "";

   /**
    * The case-sensitive name of the property to be persisted or overridden.
    */
   @Id
   @Column(name = "PROPERTYNAME", nullable = false)
   private String m_propertyName = "";

   /**
    * An arbitrary string used to group related properties together.
    * All categories beginning with sys_ are reserved by the system.
    */
   @Id
   @Column(name = "CATEGORY", nullable = false)
   private String m_category = "";

   /**
    * The current value of the property.  May be changed during the
    * property's lifetime.
    */
   @Basic
   @Column(name="PROPERTYVALUE")
   private String m_propertyValue = "";

   /**
    * Action to be taken on the property e.g update, insert, delete.
    * May be changed during the property's
    */
   @Transient
   private String m_action = "";


   /**
    * Name of the XML Element for the property
    */
   public static final String PROPERTYVALUE_ELEM = "PROPERTYVALUE";

   /**
    * Name of the table in which the properties are persisted
    */
   public static final String PERSISTEDPROPERTYVALUES_ELEM =
                                         "PERSISTEDPROPERTYVALUES";
   /**
    * Name of the XML Element that stores the context value
    */
   public static final String CONTEXT_ELEM = "CONTEXT";

   public static final String DBACTION = "DBActionType";

   /**
    * Not currently used (?)
    * @todo remove ROOTVALUE_ELEM
    */
   public static final String ROOTVALUE_ELEM = "PERSISTEDPROPERTYVALUESSET";
}
