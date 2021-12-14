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
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * This class is responsible for managing a single name/value pairing known
 * as a property. An empty class MUST be derived so that the
 * <code>getComponentType</code> method returns a different value for each
 * one. This allows this class to support the same properties model with
 * the data stored in different tables.
 * <p>Property objects are created with a name which will never change over
 * the lifetime of the object.
 * <p>Components with these types of properties should use the {@link
 * PSDbComponentCollection} for storing them.
 *
 * @author Paul Howard
 * @version 1.0
 */
public abstract class PSCmsProperty extends PSDbComponent
{
   /**
    * If supplied to ctor, specifies that the value of the name is to be
    * supplied as the value of the first part of the key when it is assigned.
    */
   public static final int KEYASSIGN_NAME_AS_KEYPART = 1;

   /**
    * If supplied to ctor, specifies that the value of the 'value' is to be
    * supplied as the value of the next part of the key when it is assigned.
    * In other words, if the KEYASSIGN_NAME_AS_KEYPART was specified in
    * addition to this flag, the content of the property value would be used
    * when assigning the key and it would be assigned to the 2nd part of the
    * key.
    */
   public static final int KEYASSIGN_VALUE_AS_KEYPART = 2;

   /**
    * The OR'd value of all possible key assignment types.
    */
   public static final int KEYASSIGN_ALL = KEYASSIGN_NAME_AS_KEYPART
         | KEYASSIGN_VALUE_AS_KEYPART;;

   /**
    * Convenience method that calls {@link #PSCmsProperty(PSKey,String,String,
    * String,int) PSCmsProperty(locator, name, null, null, 0)}.
    */
   protected PSCmsProperty(PSKey locator, String name)
   {
      this(locator, name, null, null, 0);
   }

   /**
    * default ctor
    */
   PSCmsProperty()
   {
   }

   /**
    * Convenience method that calls {@link #PSCmsProperty(PSKey,String,String,
    * String,int) PSCmsProperty(locator, name, null, null, keyControl)}.
    */
   protected PSCmsProperty(PSKey locator, String name, int keyControl)
   {
      this(locator, name, null, null, keyControl);
   }


   /**
    * Creates a object with the given parameters. The state of the object is
    * unmodified if the state of the locator is persisted.
    *
    * @param locator The key for this property, may not be <code>null</code>.
    *
    * @param name The property name, may not be <code>null</code> or empty.
    *
    * @param value The property value, may be <code>null</code> or empty.
    *
    * @param desc The property description, may be <code>null</code> or empty.
    *
    * @param keyControl  Specifies how the component should control key
    *    assignment. Passing 0 means use the default technique (seperate
    *    column for primary key). Otherwise, OR 1 or more of the KEYASSIGN_xxx
    *    flags. See description of those flags for more details.
    */
   protected PSCmsProperty(PSKey locator, String name, String value,
      String desc, int keyControl)
   {
      super(locator);

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_strName = name;
      m_strValue = (value == null) ? "" : value;
      m_strDescription = (desc == null) ? "" : desc;
      m_keyControl = keyControl & KEYASSIGN_ALL;
   }


   /**
    * For derived classes instantiating from a previously serialized instance.
    * See base class form of this ctor for more details.
    * <p>See fromXml for more details.
    *
    * @param src Never <code>null</code>.
    */
   protected PSCmsProperty(Element src)
      throws PSUnknownNodeTypeException
   {
      super(src); //validates src
      fromXml(src);
   }

   /**
    * This is for use by derived classes when constructing from xml. After this
    * method is called, the fromXml method MUST be called or the object will
    * not be in a valid state.
    *
    * @param locator  Never <code>null</code>.
    *
    * @param src Never <code>null</code>.
   protected PSCmsProperty(PSKey locator)
      throws PSUnknownNodeTypeException
   {
      super(locator);
   }
    */

   /**
    * Overridden to assign key values based on the values of this property.
    * If the property name
    * @param gen
    * 
    * @throws PSCmsException if an error occurs
    */
   protected String[] getKeyPartValues(IPSKeyGenerator gen)
      throws PSCmsException
   {
      if (m_keyControl == 0)
         return super.getKeyPartValues(gen);

      List keyParts = new ArrayList();
      if ((m_keyControl & KEYASSIGN_NAME_AS_KEYPART) == 0)
         keyParts.add(m_strName);
      if ((m_keyControl & KEYASSIGN_VALUE_AS_KEYPART) == 0)
         keyParts.add(m_strValue);
      String[] keyValues = new String[keyParts.size()];
      keyParts.toArray(keyValues);
      return keyValues;
   }


   /**
    * A value is an arbitrary text string.
    *
    * @return The value previous set w/ {@link #setValue(String) setValue},
    *    or "" if never set. Never <code>null</code>.
    */
   public String getValue()
   {
      return m_strValue;
   }

   /**
    * See {@link #getValue()} for details.
    *
    * @param value  The value you wish to store. If <code>null</code> is
    *    supplied, the empty string is stored and returned when the property
    *    is retrieved.
    */
   public void setValue(String value)
   {
      if (value == null)
         value = "";

      if (! m_strValue.equals(value))
      {
         m_strValue = value;
         setDirty();
      }
   }

   /**
    * A description is an arbitrary text string.
    *
    * @return The value previous set w/ {@link #setDescription(String)
    *    setValue}, or "" if never set. Never <code>null</code>.
    */
   public String getDescription()
   {
      return m_strDescription;
   }


   /**
    * See {@link #getDescription()} for details.
    *
    * @param value  The description you wish to store. If <code>null</code> is
    *    supplied, the empty string is stored and returned when the property
    *    is retrieved.
    */
   public void setDescription(String value)
   {
      if (value == null)
         value = "";

      if (! m_strDescription.equals(value))
      {
         m_strDescription = value;
         setDirty();
      }
   }

   /**
    * Get the name of the property.
    *
    * @return The name of the property, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_strName;
   }

   /**
    * See {@link IPSCmsComponent#toXml(Document) interface} for details.
    * The DTD for this class is as follows:
    * <pre><code>
    *    &lt;!ELEMENT getNodeName() (getLocator().getNodeName(), Value+,
    *       Description? )&gt;
    *    &lt;!ATTLIST getNodeName()
    *       state (DBSTATE_xxx)
    *       propName CDATA #REQUIRED
    *       &gt;
    * </code></pre>
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // base class toXml
      Element root = super.toXml(doc);

      root.setAttribute(XML_ATTR_PROPERTYNAME, m_strName);
      if (m_keyControl != 0)
         root.setAttribute(XML_ATTR_KEYCONTROL, ""+m_keyControl);

      Iterator it = getValues().iterator();
      while (it.hasNext())
      {
         Element elVal = PSXmlDocumentBuilder.addElement(
            doc, root, XML_NODE_PROPERTYVALUE, (String) it.next());

         if (elVal == null)
            throw new IllegalStateException(
               "Unable to create " + XML_NODE_PROPERTYVALUE + " element.");
      }

      if (m_strDescription.length() > 0)
      {
         Element elDescName = PSXmlDocumentBuilder.addElement(
            doc, root, XML_NODE_DESCRIPTION, m_strDescription);

         if (elDescName == null)
            throw new IllegalStateException(
               "Unable to create " + XML_NODE_DESCRIPTION + " element.");
      }

      return root;
   }


   /**
    * This method is provided to allow a derived class to easily implement
    * a multi-valued property. During the toXml processing, this method
    * will be called. For every entry in the returned collection, a Value
    * node will be created.
    *
    * @return Never <code>null</code>. Must contain at least 1 entry.
    *    All entries must be of type String.
    */
   protected Collection getValues()
   {
      Collection c = new ArrayList();
      c.add(getValue());
      return c;
   }

   //see interface for description
   public void fromXml(Element src)
      throws PSUnknownNodeTypeException
   {
      // Base class from xml
      super.fromXml(src);

      Element kEl = PSXMLDomUtil.getFirstElementChild(src); // skip the key el

      m_strName = PSXMLDomUtil.checkAttribute(src, XML_ATTR_PROPERTYNAME, true);
      int keyControl =
            PSXMLDomUtil.checkAttributeInt(src, XML_ATTR_KEYCONTROL, false);
      if (keyControl >= 0)
         m_keyControl = keyControl & KEYASSIGN_ALL;

      Collection values = new ArrayList();
      Element el = PSXMLDomUtil.getNextElementSibling(kEl);
      while (el != null && el.getTagName().equals(XML_NODE_PROPERTYVALUE))
      {
         values.add(PSXMLDomUtil.getElementData(el));
         el = PSXMLDomUtil.getNextElementSibling(el);
      }
      if (values.size() == 0)
      {
         String[] args =
         {
            getNodeName(),
            "Value",
            "missing node"
         };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      setValues(values);

      if (el != null && el.getTagName().equals(XML_NODE_DESCRIPTION))
         m_strDescription = PSXMLDomUtil.getElementData(el);
      else
         m_strDescription = "";
   }

   /**
    * This method is provided to allow a derived class to easily implement
    * a multi-valued property. During the fromXml processing, this method
    * will be called with all of the contents of the Value nodes.
    *
    * @param values Never <code>null</code>. Must contain at least 1 entry.
    *    All entries must be of type String. This class only uses the first
    *    entry.
    *
    * @see #getValues
    */
   protected void setValues(Collection values)
   {
      if (null == values || values.size() == 0)
         throw new IllegalArgumentException("Must supply at least 1 value.");
      m_strValue = (String) values.iterator().next();
   }

   //see interface for description
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
         PSKey parent)
      throws PSCmsException
   {
      super.toDbXml(doc, root, keyGen, parent);
   }

   //see base class for description
   public Object clone()
   {
      PSCmsProperty copy = (PSCmsProperty) super.clone();
      copy.m_strName = m_strName;
      copy.m_strValue = m_strValue;
      copy.m_strDescription = m_strDescription;

      return copy;
   }

   //see base class for description
   public Object cloneFull()
   {
      PSCmsProperty copy = (PSCmsProperty) super.cloneFull();
      copy.m_strName = m_strName;
      copy.m_strValue = m_strValue;
      copy.m_strDescription = m_strDescription;

      return copy;
   }

   //see base class for description
   public boolean equals(Object obj)
   {
      // Base class shot - checks null and instance
      if (!super.equals(obj))
         return false;

      PSCmsProperty comp = (PSCmsProperty) obj;

      return compare(m_strName, comp.m_strName)
         && compare(m_strDescription, comp.m_strDescription)
         && compare(m_strValue, comp.m_strValue);
   }

   //see base class for description
   public boolean equalsFull(Object obj)
   {
      if (equals(obj))
         return super.equalsFull(obj);
      else
         return false;
   }

   //see base class for description
   public int hashCode()
   {
      // Base class shot
      int nHash = super.hashCode();

      return nHash +
         m_strName.hashCode() +
         m_strDescription.hashCode() +
         m_strValue.hashCode();
   }

   // private defins
   private static final String KEY_COL_PROPERTYID = "ID";

   // public static defines
   public static final String XML_ATTR_PROPERTYNAME = "propName";
   public static final String XML_ATTR_KEYCONTROL = "keyControl";
   public static final String XML_NODE_PROPERTYVALUE = "Value";
   public static final String XML_NODE_DESCRIPTION = "Description";


   /**
    * The name of the object, initialized in definition and never <code>null
    * </code>, empty or modified after that.
    */
   protected String m_strName = "";

   /**
    * The value of the object, initialized in definition and may be <code>null
    * </code> or modified throught a call to <code>setValue(Object)</code>
    * after that.
    */
   protected String m_strValue = "";

   /**
    * The description of the object, initialized in definition and may be
    * <code> null</code>, empty or modified after that.
    */
   protected String m_strDescription = "";

   /**
    * Flags that determine how the key is assigned. Set during construction,
    * then never changes after that (exception fromXml). Default value is 0.
    * The value is an OR'd collection of 0 or more of the KEYASSIGN_xxx values.
    */
   private int m_keyControl = 0;
}
