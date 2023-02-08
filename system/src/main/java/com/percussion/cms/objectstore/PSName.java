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
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This is an abstract class that implements a simple name entry in the
 * database. A name entry has an internal name, external (or display) name
 * and a description. This class also enforces a read-only behavior on objects
 * derived from it. A read-only component is one that can be instantiated from
 * the database, but cannot be modified, and thus written to the db.
 * <p>It implements all modification methods (except fromXml) by overriding
 * them and throwing an UnsupportedOperationException. toDbXml is a noop,
 * therefore setPersisted and setDeleted are as well.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSName extends PSDbComponent
{
   /**
    * The only ctor.
    *
    * @param key Never <code>null</code>.
    */
   protected PSName(PSKey key)
   {
      super(key);
   }

   //*>>>debug
   protected PSName(PSKey key, String name, String dname, String desc)
   {
      super(key);
      m_name = name;
      m_displayName = dname;
      m_description = desc == null ? "" : desc;
   }
   //*///<<<debug

   /**
    * An internal identifier for this mode. Use getDisplayName() for an
    * external identifier.
    *
    * @return Never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * An external identifier for this mode. Use getName() for an internal
    * identifier.
    *
    * @return Never <code>null</code> or empty.
    */
   public String getDisplayName()
   {
      return m_displayName;
   }


   //see interface/base class for description
   public String getDescription()
   {
      return m_description;
   }


   /**
    * See interface/base class for description.
    * The dtd (based on the base class) is:
    * <pre><code>
    *    &lt;!ELEMENT getNodeName() (getLocator().getNodeName(),
    *       Description?)&gt;
    *    &lt;!ATTLIST getNodeName()
    *       state (DBSTATE_xxx)
    *       name CDATA #REQUIRED
    *       displayName CDATA #REQUIRED
    *       &gt;
    *    &lt;!ELEMENT Description (#PCDATA)&gt;
    * </code></pre>
    */
   public Element toXml(Document doc)
   {
      Element root = super.toXml(doc);
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_DISPLAYNAME, m_displayName);
      if (m_description.length() > 0)
      {
         PSXmlDocumentBuilder.addElement(doc, root, XML_ELEM_DESCRIPTION,
               m_description);
      }
      return root;
   }


   //see interface/base class for description
   public void fromXml(Element source)
      throws PSUnknownNodeTypeException
   {
      super.fromXml(source);
      m_name = PSXMLDomUtil.checkAttribute(source, XML_ATTR_NAME, true);
      m_displayName = PSXMLDomUtil.checkAttribute(source, XML_ATTR_DISPLAYNAME,
            true);

      Element kEl = PSXMLDomUtil.getFirstElementChild(source); // skip the key
      Element el = PSXMLDomUtil.getNextElementSibling(kEl);
      if (null != el)
         m_description = PSXMLDomUtil.getElementData(el);
   }


   //see interface/base class for description
   public boolean equalsFull(Object obj)
   {
      if (!equals(obj))
         return false;
      else if (!super.equalsFull(obj))
         return false;
      return true;
   }


   //see interface/base class for description
   public boolean equals(Object obj)
   {
      if (!super.equals(obj))
         return false;

      PSName other = (PSName) obj;

      if (!m_name.equalsIgnoreCase(other.m_name))
         return false;
      else if (!m_displayName.equalsIgnoreCase(other.m_displayName))
         return false;
      else if (!m_description.equalsIgnoreCase(other.m_description))
         return false;

      return true;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }

   /**
    * This class is read-only.
    *
    * @throws UnsupportedOperationException Always.
    */
   public void setState(String parm1)
   {
      throw new UnsupportedOperationException("This object is read only.");
   }


   /**
    * This class never modifies the db, so this action is a noop.
    */
   public void setDeleted()
   {
   }


   /**
    * This class never modifies the db, so this action is a noop.
    */
   public void setPersisted()
      throws PSCmsException
   {
   }


   //constants for element/attribute names
   public static final String XML_ATTR_NAME = "name";
   public static final String XML_ATTR_DISPLAYNAME = "displayName";
   public static final String XML_ELEM_DESCRIPTION = "Description";

   /**
    * The internal name for this mode. Never <code>null</code>, empty or
    * modified after construction.
    */
   private String m_name;

   /**
    * The name shown to the implmentors.Never <code>null</code>, empty or
    * modified after construction.
    */
   private String m_displayName;

   /**
    * How is this mode used. Never <code>null</code> or modified after
    * construction. May be empty. Defaults to "".
    */
   private String m_description = "";


   /**
    * Does nothing.
    */
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
         PSKey parent)
      throws PSCmsException
   {
   }


   /**
    * This class is read-only.
    *
    * @throws UnsupportedOperationException Always.
    */
   public void setLocator(PSKey locator)
   {
      throw new UnsupportedOperationException("This object is read only.");
   }
}
