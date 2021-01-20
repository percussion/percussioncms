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

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Security provider instance summary returned by the SecurityProvider cataloger.
 */
public class PSSecurityProviderInstanceSummary implements IPSCmsComponent
{

   /**
    * Constructs a new <code>PSSecurityProvider</code> object.
    * Used only by this classes clone method.
    */
   private PSSecurityProviderInstanceSummary() {
   }

   /**
    * Constructs a new <code>PSSecurityProvider</code> object.
    * @param src the provider element node to create this
    * object from. May not be <code>null</code>.
    */
   public PSSecurityProviderInstanceSummary(PSSecurityProviderInstance instance)
   {
       m_type = instance.getType();
       m_name = PSSecurityProvider.getSecurityProviderTypeString(m_type);
       m_instance = instance.getName();
   }

   /**
    * Constructs a new <code>PSSecurityProvider</code> object.
    * @param src the provider element node to create this
    * object from. May not be <code>null</code>.
    */
   public PSSecurityProviderInstanceSummary(Element src)
   {
      try
      {
         fromXml(src);
      }
      catch(PSUnknownNodeTypeException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Returns this object as xml
    * <code><pre>
    *
    *   &lt;!ELEMENT PSXSecurityProviderInstanceSummary (name)&gt;
    *   &lt;!ATTLIST
    *    typeName  CDATA       #REQUIRED
    *    typeId    CDATA       #REQUIRED
    *   &gt;
    *
    *  &lt;!ELEMENT name       (#PCDATA)&gt;
    *
    * </pre></code>
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException(
            "doc may not be null");

      Element root = doc.createElement(XML_ELEMENT_ROOT);
      root.setAttribute(XML_ATTRIB_TYPEID, String.valueOf(m_type));
      root.setAttribute(XML_ATTRIB_TYPENAME, m_name);
      Element nameElem = doc.createElement(XML_ELEMENT_NAME);
      nameElem.appendChild(doc.createTextNode(m_instance));
      root.appendChild(nameElem);

      return root;
   }

   /**
    * Sets this objects values from xml
    * @param src xml element node
    * from the following DTD.
    * <code><pre>
    *
    *   &lt;!ELEMENT PSXSecurityProviderInstanceSummary (name)&gt;
    *   &lt;!ATTLIST
    *    typeName  CDATA       #REQUIRED
    *    typeId    CDATA       #REQUIRED
    *   &gt;
    *
    *  &lt;!ELEMENT name       (#PCDATA)&gt;
    *
    * </pre></code>
    * @throws PSUnknownNodeTypeException
    */
   public void fromXml(Element src) throws PSUnknownNodeTypeException
   {
        if(null == src)
          return;

        m_type = Integer.parseInt(src.getAttribute(XML_ATTRIB_TYPEID));
        m_name = src.getAttribute(XML_ATTRIB_TYPENAME);

        NodeList nodes = src.getElementsByTagName(XML_ELEMENT_NAME);
        if(null == nodes)
        {
          String[] array =
             {"PSSecurityProviderInstanceSummary.fromXml node: " + XML_ELEMENT_NAME};
           throw new PSUnknownNodeTypeException(
              IPSObjectStoreErrors.XML_ELEMENT_NULL, array);
        }
        m_instance = ((Text)nodes.item(0).getFirstChild()).getData();

   }

   /**
    * Returns the root node name for this object
    * @return the root node name. Never <code>null</code>.
    */
   public String getNodeName()
   {
      return XML_ELEMENT_ROOT;
   }

   /**
    * Returns a deep copy of this object
    * @return copy of this object. Never <code>null</code>.
    */
   public Object clone()
   {
      PSSecurityProviderInstanceSummary copy = null;

      copy = new PSSecurityProviderInstanceSummary();
      copy.m_type = m_type;
      copy.m_name = m_name;
      copy.m_instance = m_instance;

      return copy;
   }

   /**
    * An object is equal if it is an instance of
    * <code>PSSecurityProviderInstanceSummary</code>
    * and the type id's match
    * @param obj object to test equality on
    * @return <code>true</code> if equals, elese <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      if(!(obj instanceof PSSecurityProviderInstanceSummary))
         return false;
      PSSecurityProviderInstanceSummary provider =
         (PSSecurityProviderInstanceSummary)obj;
      return m_type == provider.getTypeId() &&
         m_instance.equals(provider.getInstanceName());
   }

   /**
    * Returns the the security provider type id
    * @return type id
    */
   public int getTypeId()
   {
      return m_type;
   }

   /**
    * Returns the the security provider type name
    * @return type name. May be <code>null</code>.
    */
   public String getTypeName()
   {
      return m_name;
   }

   /**
    * Returns name of instance for this provider
    * @return name of instance. May be <code>null</code>or empty.
    */
   public String getInstanceName()
   {
      return m_instance;
   }

   /**
    * Returns the providers instance name as the string
    * representation of this object
    * @return the name string.
    */
   public String toString()
   {
      return m_instance;
   }

   /**
    * Returns unique hashcode for this object
    * instance.
    * @return unique hashcode.
    */
   public int hashCode()
   {
      return m_instance.hashCode() + m_type;
   }

   /**
    * The security provider type id. Initialized in ctor.
    */
   private int m_type;

   /**
    * The the name of the security provider. Initialized in ctor.
    * May be <code>null</code>.
    */
   private String m_name;

   /**
    * Instance name. May be <code>null</code>.
    */
   private String m_instance;

   /**
    * Security provider xml root element node name
    */
   public static final String XML_ELEMENT_ROOT = "PSXSecurityProviderInstanceSummary";

   /**
    * Security provider xml name element node name
    */
   public static final String XML_ELEMENT_NAME = "name";

   /**
    * Security provider xml type id attribute
    */
   public static final String XML_ATTRIB_TYPEID = "typeId";

   /**
    * Security provider xml type name attribute
    */
   public static final String XML_ATTRIB_TYPENAME = "typeName";
}