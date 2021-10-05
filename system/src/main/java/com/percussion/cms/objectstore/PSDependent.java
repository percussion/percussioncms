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

import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSPropertySet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * This object encapsulates a locator together with properties.
 */
public class PSDependent extends PSCmsComponent
{
   /**
    * Create a new dependent for the supplied relationship id and locator.
    * 
    * @param relationshipId the relationship id
    * @param locator the dependent locator, cannot be <code>null</code>.
    */
   public PSDependent(int relationshipId, PSLocator locator)
   {
      this(relationshipId, locator, null);
   }
   
   /**
    * Create a new dependent for the supplied locator and properties.
    * 
    * @param relationshipId the relationship id
    * @param locator the dependent locator, not <code>null</code>.
    * @param properties the depedent properties, may be <code>null</code> or
    *    empty.
    */
   public PSDependent(int relationshipId, PSLocator locator, 
      PSPropertySet properties)
   {
      setRelationshipId(relationshipId);
      setLocator(locator);
      setProperties(properties);
   }
   
   /**
    * Creates an instance from a previously serialized (using <code>toXml
    * </code>) one.
    *
    * @param source a valid element that meets the dtd defined in the
    *    description of {@link #toXml(Document)}. Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the supplied source element does
    *    not conform to the DTD defined in the <code>fromXml<code> method.
    */
   public PSDependent(Element source) throws PSUnknownNodeTypeException
   {
      fromXml(source, null, null);
   }

   /**
    * This is the name of the root element in the serialized version of this
    * object.
    *
    * @return the root name, never <code>null</code> or empty.
    */
   public static String getNodeName()
   {
      return "PSXDependent";
   }
   
   /**
    * Get the relationship id.
    * 
    * @return the relationship id.
    */
   public int getRelationshipId()
   {
      return m_relationshipId;
   }
   
   /**
    * Set a new relationship id.
    * 
    * @param relationshipId the new relationship id.
    */
   public void setRelationshipId(int relationshipId)
   {
      m_relationshipId = relationshipId;
   }

   /**
    * Get the locator.
    * 
    * @return the locator, never <code>null</code>.
    */
   public PSLocator getLocator()
   {
      return m_locator;
   }
   
   /**
    * Set a new locator.
    * 
    * @param locator the new locator, not <code>null</code>.
    */
   public void setLocator(PSLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator cannot be null");
      
      m_locator = locator;
   }
   
   /**
    * Get the dependents properties.
    * 
    * @return the dependent properties, never <code>null</code>, may be empty.
    */
   public PSPropertySet getProperties()
   {
      return m_properties;
   }
   
   /**
    * Set the dependent properties.
    * 
    * @param properties the new dependent properties, may be <code>null</code>
    *    or empty.
    */
   public void setProperties(PSPropertySet properties)
   {
      if (properties == null)
         m_properties = new PSPropertySet();
      else
         m_properties = properties;
   }

   /**
    * Overrides the base class to compare each of the members. All
    * members are compared for exact matches.
    *
    * @return <code>true</code> if all members are equal, otherwise 
    *    <code>false</code> is returned.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSDependent))
         return false;

      PSDependent dependent = (PSDependent) o;

      if (m_relationshipId != dependent.m_relationshipId)
         return false;
      if (!m_locator.equals(dependent.m_locator))
         return false;
      if (m_properties.equals(dependent.m_properties))
         return false;

      return true;
   }

   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following DTD:
    * <pre>
    * <!ELEMENT PSXDependent (PSXLocator, PSXPropertySet?)>
    * <ATTRIBUTE PSXDependent relationshipId>
    * </pre>
    *
    * @see IPSComponent for additional information.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must be supplied");

      Element root  = doc.createElement(getNodeName());
      root.setAttribute("relationshipId", "" + getRelationshipId());
      root.appendChild(getLocator().toXml(doc));

      if (!getProperties().isEmpty())
         root.appendChild(getProperties().toXml(doc));

      return root;
   }

   /**
    * Constructs a new object from its XML representation. The DTD expected is:
    * <pre>
    * <!ELEMENT PSXDependent (PSXLocator, PSXPropertySet?)>
    * <ATTRIBUTE PSXDependent relationshipId>
    * </pre>
    * 
    * @see IPSComponent for additional information.
    */
   public void fromXml(Element source, IPSDocument parentDoc, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source must be supplied");

      // make sure we got the correct root node tag
      if (!getNodeName().equals(source.getNodeName()))
      {
         Object[] args = { getNodeName(), source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(source);

      setRelationshipId(
         PSXMLDomUtil.checkAttributeInt(source, "relationshipId", true));

      setLocator(
         new PSLocator(walker.getNextElement(PSLocator.XML_NODE_NAME)));
      
      Element properties = walker.getNextElement(PSPropertySet.XML_NODE_NAME);
      setProperties((properties == null) ? null : new PSPropertySet(properties));
   }

   /**
    * Must be overridden to properly fulfill the contract.
    *
    * @return a value computed by adding the hash codes of all members.
    */
   public int hashCode()
   {
      return m_relationshipId + m_locator.hashCode() + m_properties.hashCode();
   }
   
   /**
    * The dependent relationship id. Initialized in constructor, may be changed 
    * through {@link #setRelationshipId(int)}.
    */
   private int m_relationshipId = -1;

   /**
    * The dependent locator. Initialized in constructor. Never <code>null</code>
    * after that, may be changed through {@link #setLocator(PSLocator)}.
    */
   private PSLocator m_locator = null;
   
   /**
    * The dependent properties, Initialized in constructor, never 
    * <code>null</code> after that, may be empty. May be changed through
    * {@link #setProperties(PSPropertySet)}.
    */
   private PSPropertySet m_properties = null;
}
