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

import com.percussion.cms.objectstore.PSComponentUtils;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.List;

/**
 * This class represents a field that is overridden by clone handler during 
 * creation of a clone of a CMS object and has the following properties:
 * <p>
 * It has a name which corresponds to one of the content editor fields. The 
 * value of this field will be an object implementing 
 * {@link IPSReplacementValue replacement value} interface. Consists of a list 
 * of {@link PSRule conditionals} to facilitate conditional override of 
 * the field for the clone. 
 *    
 * @author RammohanVangapalli
 *
 */
public class PSCloneOverrideField extends PSComponent
{
   /**
    * Constructs a clone field from supplied name and the replacement value.
    *
    * @param name the name of the content ediotor field to override in a 
    *    newly created clone, must not be <code>null</code> or empty.
    * @param replacememtValue the dynamic value for the field, must not be 
    *    <code>null</code>.
    */
   public PSCloneOverrideField(String name, 
      IPSReplacementValue replacememtValue)
   {
      setName(name);
      setReplacementValue(replacememtValue);
   }

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param sourceNode the XML element node to construct this object from.
    * @param parentDoc the Java object which is the parent of this object.
    * @param parentComponents the parent objects of this object.
    * @throws PSUnknownNodeTypeException if the XML element node is not of the 
    *    appropriate type.
    */
   public PSCloneOverrideField(Element sourceNode, IPSDocument parentDoc,
      List parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Produces an XML element which conforms to the 
    * <code>PSXCloneOverrideField </code> element defined in the 
    * sys_RelationshipConfig.dtd.
    * 
    * @see IPSComponent#toXml(Document)
    */
   public Element toXml(Document doc)
   {
      Element result = doc.createElement(XML_NODE_NAME);
      result.setAttribute("name", m_name);
      result.setAttribute(ID_ATTR, String.valueOf(m_id));
      
      Element valueElem = doc.createElement("value");
      valueElem.appendChild(((IPSComponent) m_replacementValue).toXml(doc));
      result.appendChild(valueElem);
      
      Element rulesElem = doc.createElement("Conditions");
      result.appendChild(rulesElem);
      Iterator iter = m_rules.iterator();
      while (iter.hasNext())
      {
         PSRule rule = (PSRule) iter.next();
         rulesElem.appendChild(rule.toXml(doc));
      }
      return result;
   }

   /* (non-Javadoc)
    * @see IPSComponent#fromXml(Element, IPSDocument, ArrayList)
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      List parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null.");

      parentComponents = updateParentList(parentComponents);

      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      try
      {
         m_id = Integer.parseInt(sourceNode.getAttribute(ID_ATTR));
      }
      catch (Exception e)
      {
         //ignore         
      }
      m_name = PSComponentUtils.getRequiredAttribute(sourceNode, "name");

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      Element repValElem = tree.getNextElement(true);
      tree.setCurrent(repValElem);
      Element repElem = tree.getNextElement(true);
      try
      {
         setReplacementValue(
            PSReplacementValueFactory.getReplacementValueFromXml(parentDoc, 
               parentComponents, repElem, "value", "value"));
      }
      catch (IllegalArgumentException e)
      {
         Object[] args = 
         { 
            XML_NODE_NAME, 
            "value", 
            "null or not PSXExtensionCall"
         };
         
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
         
      m_rules.clear();
      NodeList nl = sourceNode.getElementsByTagName("PSXRule");
      for (int i = 0; i < nl.getLength(); i++)
      {
         Element elem = (Element) nl.item(i);
         PSRule rule = new PSRule(elem, parentDoc, null);
         m_rules.add(rule);
      }
   }

   /**
    * Access method for the name of the field.
    * 
    * @return name of the field to override, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set name of the field.
    * 
    * @param name name of the field must not be <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name must not be null or empty");
         
      m_name = name;
   }

   /**
    * Get the list of rules for overriding this field. The collection 
    * can be modifed by the caller.
    * 
    * @return list of rules. Each entry in the collection is a 
    *    {@link PSRule rule object}, never <code>null</code>, may be empty.
    */
   public PSCollection getRules()
   {
      return m_rules;
   }
   
   /**
    * Sets a list of rules.
    * 
    * @param rules list of rules. Each entry in the collection must be of type
    *    {@link PSRule rule object}, not <code>null</code>, may be empty.
    */
   public void setRules(PSCollection rules)
   {
      if (rules == null)
         throw new IllegalArgumentException("rules cannot be null");
      
      if (!rules.getMemberClassType().equals(PSRule.class))
         throw new IllegalArgumentException(
            "rules must be of type PSRule.class");
      
      m_rules = rules;
   }

   /**
    * Get the replacement or dynamic value for the field.
    * 
    * @return replacement value, never <code>null</code>, guaranteed of type 
    *    <code>PSExtensionCall</code>.
    */
   public IPSReplacementValue getReplacementValue()
   {
      return m_replacementValue;
   }

   /**
    * Set the replacement or dynamic value for the field to be overridden.
    * 
    * @param value replacement value, not <code>null</code>, must be of type
    *    <code>PSExtensionCall</code>.
    */
   public void setReplacementValue(IPSReplacementValue value)
   {
      if (!(value instanceof PSExtensionCall))
         throw new IllegalArgumentException(
            "value must not be null and must be of type PSExtensionCall");
      
      m_replacementValue = value;
   }
   
   /**
    * Returns m_replacementValue.getValueDisplayText().
    * 
    * @return m_replacementValue.getValueDisplayText(),
    * or empty string if m_replacementValue is not set. 
    */
   public String toString()
   {
      return m_replacementValue == null ? 
         "" : m_replacementValue.getValueDisplayText();
   }
   
   // see interface for description
   public Object clone()
   {
      PSCloneOverrideField field = new PSCloneOverrideField(m_name, 
         (IPSReplacementValue) m_replacementValue.clone());
      field.setRules((PSCollection) m_rules.clone());
         
      return field;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      if (! (obj instanceof PSCloneOverrideField))
         return false;

      PSCloneOverrideField other = (PSCloneOverrideField) obj;

      return new EqualsBuilder()
         .append(m_name, other.m_name)
         .append(m_replacementValue, other.m_replacementValue)
         .append(m_rules, other.m_rules)
         .isEquals();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return new HashCodeBuilder(23, 4).appendSuper(super.hashCode())
            .append(m_name)
            .append(m_replacementValue)
            .append(m_rules)
            .toHashCode();
   }



   /**
    * Name of the field whose value needs to be overridden. Initialized via 
    * one of the constructors or in {@link #fromXml(Element, IPSDocument, 
    * List)} or using {@link PSCloneOverrideField#setName(String)} methods.
    * Never <code>null</code> or empty after initialization.
    */
   private String m_name = null;

   /**
    * Dynamic value for the field. Never <code>null</code>, guaranteed of 
    * type <code>PSExtensionCall</code>.
    */
   private IPSReplacementValue m_replacementValue = null;

   /**
    * List of rules that can be evaluated whether to override this 
    * field or not. Never <code>null</code> but may be empty.
    */
   private PSCollection m_rules = new PSCollection(PSRule.class);

   /**
    * Name of the root element in the XML representation of this object.  
    */
   static public final String XML_NODE_NAME = "PSXCloneOverrideField";
}
