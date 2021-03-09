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
package com.percussion.cx.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * The class that is used to represent properties as defined by
 * 'sys_Params.dtd'.
 */
public class PSParameters implements IPSComponent
{
   /**
    * The default constructor to create parameters with empty list.
    */
   public PSParameters()
   {
   }

   /**
    * Constructs this object from the supplied element. See {@link
    * #toXml(Document) } for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if element is <code>null</code>
    * @throws PSUnknownNodeTypeException if element is invalid.
    */
   public PSParameters(Element element) throws PSUnknownNodeTypeException
   {
      if(element == null)
         throw new IllegalArgumentException("element may not be null.");

      fromXml(element);
   }

   /**
    * Create this parameters as a shallow copy of the supplied parameters
    * 
    * @param other The parameters to copy from, may not be <code>null</code>.
    */
   public PSParameters(PSParameters other)
   {
      if (other == null)
         throw new IllegalArgumentException("other may not be null");
      
      for (Map.Entry<String, String> entry : other.m_params.entrySet())
      {
         m_params.put(entry.getKey(), entry.getValue());
      }
   }

   // implements interface method, see toXml(Document) for the expected format
   //of the xml element.
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if(sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null.");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      Iterator list = PSComponentUtils.getChildElements(
            sourceNode, PARAM_NODE_NAME);
      if(!list.hasNext())
      {
         Object[] args = { XML_NODE_NAME, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      m_params.clear();
      while(list.hasNext())
      {
         Node param = (Node)list.next();
         NamedNodeMap nodeMap = param.getAttributes();
         Node attrib = nodeMap.getNamedItem(NAME_ATTR);
         if(attrib == null || attrib.getNodeValue() == null ||
            attrib.getNodeValue().trim().length() == 0)
         {
            Object[] args = { PARAM_NODE_NAME, "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         if(param.getFirstChild() instanceof Text)
         {
            m_params.put(attrib.getNodeValue(),
               param.getFirstChild().getNodeValue());
         }
         else
         {
            //Treat it as empty value
            m_params.put(attrib.getNodeValue(), "");
         }
      }
   }

   /**
    * Implements the IPSComponent interface method to produce XML representation
    * of this object. See the interface for description of the method and
    * parameters.
    * <p>
    * The xml format is:
    * <pre><code>
    * &lt;!ELEMENT Params (Param+)>
    * &lt;!ELEMENT Param (#PCDATA)>
    * &lt;!ATTLIST Param
    *      name CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * @return the element, may be <code>null</code> if no parameters exist.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null.");

      Element params = null;

      if(!m_params.isEmpty())
      {
         params = doc.createElement(XML_NODE_NAME);
         Iterator parameters = m_params.entrySet().iterator();
         while(parameters.hasNext())
         {
            Map.Entry param = (Map.Entry)parameters.next();
            Element paramEl = doc.createElement(PARAM_NODE_NAME);
            paramEl.setAttribute(NAME_ATTR, (String)param.getKey());
            paramEl.appendChild(doc.createTextNode((String)param.getValue()));
            params.appendChild(paramEl);
         }
      }

      return params;
   }

   /**
    * Gets the value of the specified parameter. Uses case-sensitive comparison
    * to get the parameter.
    *
    * @param name name of the parameter, may not be <code>null</code> or empty.
    *
    * @return the parameter value, may be <code>null</code> if the specified
    * parameter does not exist or its value is <code>null</code>
    */
   public String getParameter(String name)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      return m_params.get(name);
   }

   /**
    * Sets the specified parameter with supplied value. If the parameter with
    * that name exists it will be replaced. The parameter name and values are
    * case-sensitive.
    *
    * @param name name of the parameter, may not be <code>null</code> or empty.
    * @param value value of the parameter, may be <code>null</code> or empty.
    */
   public void setParameter(String name, String value)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      m_params.put(name, value);
   }

   //implements interface method.
   public boolean equals(Object obj)
   {
      boolean equals = false;

      if( obj instanceof PSParameters )
      {
         PSParameters other = (PSParameters)obj;
         if(m_params.equals(other.m_params))
            equals = true;
      }

      return equals;
   }

   //implements interface method.
   public int hashCode()
   {
      return m_params.hashCode();
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return "<PSParameters " + m_params.toString() + ">";
   }

   /**
    * Returns all parameter keys.
    * @return iterator having keys of all parameters, never <code>null</code>.
    */
   public Iterator getParamKeys()
   {
      return m_params.keySet().iterator();
   }

   /**
    * The map of parameters with 'name' (<code>String</code>) as key and 'value'
    * as value (<code>String</code>). Initialized to an empty map and gets
    * filled as it reads from xml. May be modified through calls to <code>
    * fromXml(Element)</code>, but never <code>null</code>
    */
   private Map<String, String> m_params = new HashMap<>();

   /**
    * The constant to indicate root node name.
    */
   public static final String XML_NODE_NAME = "Params";

   //xml constants
   private static final String PARAM_NODE_NAME = "Param";
   private static final String NAME_ATTR = "name";
}
