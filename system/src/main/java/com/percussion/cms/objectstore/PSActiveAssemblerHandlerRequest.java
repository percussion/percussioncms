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
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * This class encapsulates a request to the Active Assembler Request handler.
 */
public class PSActiveAssemblerHandlerRequest extends PSCmsComponent
{
   /**
    * Constructs a new active asembler request for the supplied parameters.
    *
    * @param owner the relationship owner, not <code>null</code>.
    * @param dependents the relationship dependents, not <code>null</code>,
    *    may be empty.
    */
   public PSActiveAssemblerHandlerRequest(PSLocator owner,
      PSDependentSet dependents)
   {
      this(owner, dependents, -1);
   }

   /**
    * Constructs a new active asembler request for the supplied parameters.
    *
    * @param owner the relationship owner, not <code>null</code>.
    * @param dependents the relationship dependents, not <code>null</code>,
    *    may be empty.
    * @param index the index used for insert and reorder operations, -1 if
    *    not used.
    */
   public PSActiveAssemblerHandlerRequest(PSLocator owner,
      PSDependentSet dependents, int index)
   {
      setOwner(owner);
      setDependents(dependents);
      setIndex(index);
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
   public PSActiveAssemblerHandlerRequest(Element source)
      throws PSUnknownNodeTypeException
   {
      fromXml(source, null, null);
   }

   /**
    * Get the relationship owner for this request.
    *
    * @return the relationship owner, never <code>null</code>.
    */
   public PSLocator getOwner()
   {
      return m_owner;
   }

   /**
    * Set the relationship owner for this request.
    *
    * @param owner the new relationship owner, not <code>null</code>.
    */
   public void setOwner(PSLocator owner)
   {
      if (owner == null)
         throw new IllegalArgumentException("owner cannot be null");

      m_owner = owner;
   }

   /**
    * Get the relationship dependents of this request.
    *
    * @return the relationship dependents, never <code>null</code>, may be empty.
    */
   public PSDependentSet getDependents()
   {
      return m_dependents;
   }

   /**
    * Set the relationship dependents for this request.
    *
    * @param dependents teh new dependents, nut <code>null</code>, may be empty.
    */
   public void setDependents(PSDependentSet dependents)
   {
      if (dependents == null)
         throw new IllegalArgumentException("dependents cannot be null");

      m_dependents = dependents;
   }

   /**
    * Get the index used for this request. The index is only needed for
    * insert and reorder requests.
    *
    * @return the index used for this request, -1 means its undefined, not
    *    used.
    */
   public int getIndex()
   {
      return m_index;
   }

   /**
    * Set the new index for this request.
    *
    * @param index the new index, -1 if not used. If a number lower then -1
    *    is provided, it is set to undefined (-1).
    */
   public void setIndex(int index)
   {
      if (index < -1)
         m_index = -1;
      else
         m_index = index;
   }

   /**
    * This is the name of the root element in the serialized version of this
    * object.
    *
    * @return the root name, never <code>null</code> or empty.
    */
   public static String getNodeName()
   {
      return "PSXActiveAssemblerHandlerRequest";
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
      if (!(o instanceof PSActiveAssemblerHandlerRequest))
         return false;

      PSActiveAssemblerHandlerRequest request =
         (PSActiveAssemblerHandlerRequest) o;

      if (!m_owner.equals(request.m_owner))
         return false;
      if (!m_dependents.equals(request.m_dependents))
         return false;
      if (m_index != request.m_index)
         return false;

      return true;
   }

   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the
    * PSXActiveAssemblerHandlerRequest.dtd.
    *
    * @see IPSComponent for additional information.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must be supplied");

      Element root  = doc.createElement(getNodeName());
      root.setAttribute("index", Integer.toString(getIndex()));
      root.appendChild(getOwner().toXml(doc));
      root.appendChild(getDependents().toXml(doc));

      return root;
   }

   /**
    * Constructs a new object from its XML representation. The DTD expected is
    * defined in PSXActiveAssemblerHandlerRequest.dtd.
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

      int index = -1;
      String strIndex = source.getAttribute("index");
      if (strIndex != null)
      {
         try
         {
            index = Integer.parseInt(strIndex);
         }
         catch (NumberFormatException e)
         {
            // ignore
         }
      }
      setIndex(index);

      setOwner(new PSLocator(
         walker.getNextElement(PSLocator.XML_NODE_NAME)));
      setDependents(new PSDependentSet(
         walker.getNextElement(PSDependentSet.getNodeName()), null, null));
   }

   /**
    * Must be overridden to properly fulfill the contract.
    *
    * @return a value computed by adding the hash codes of all members.
    */
   public int hashCode()
   {
      return m_owner.hashCode() + m_dependents.hashCode() +
         Integer.toString(m_index).hashCode();
   }

   /**
    * The owner locator for this request, initialized during construction,
    * never <code>null</code> after that. May be changed through
    * {@link #setOwner(PSLocator)}.
    */
   private PSLocator m_owner = null;

   /**
    * A list of dependents for this request, initialized during construction,
    * never <code>null</code>, may empty after that. May be changed through
    * {@link #setDependents(PSDependentSet)}.
    */
   private PSDependentSet m_dependents = null;

   /**
    * The index for this request. initialized during construction. May be
    * changed through {@link #setIndex(int)}. -1 if not used.
    */
   private int m_index = -1;
}
