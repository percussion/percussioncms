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
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The slot is basically a container for other items with particular formatting
 * (variants). It only allows a specified set of content type/variants to be
 * attached with certain types of relationships. These attached items are known
 * as related content.
 * <p>This class doesn't override the clone method because the clone provided
 * by the Object base class is satisfactory.
 */
public class PSSlot extends PSCmsComponent
{
   /**
    * Creates an instance. Related content can be added after creation using
    * the <code>addRelatedItem</code> method.
    *
    * @param name The name for the associated slot. Never
    *    <code>null</code> or empty. Names are treated in a case-insensitive
    *    manner.
    *
    * @param id The unique numeric identifier for the slot.
    *
    * @param description An optional message that describes what the
    *    slot is used for. May be <code>null</code> or empty.
    *
    * @param allowedVariants For each entry, the key is an Integer wrapping the
    *    content type id while the value is an array of int. Each int is the
    *    variant id allowed for that associated content type.
    */
   public PSSlot(String name, int id, String description,
         Map allowedVariants)
   {
      m_name = name;
      m_slotId = id;
      m_description = description;

      //copy to our own map for protection
      Iterator entries = allowedVariants.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry) entries.next();
         m_allowedVariants.put( entry.getKey(), entry.getValue());
      }
   }

   /**
    * Convenience method. Equivalent to calling {@link #addRelatedItem(
    * PSLocator,int) addRelatedItem(item, Integer.MAX_VALUE)}.
    */
   public void addRelatedItem( PSLocator item )
   {
      m_relatedItems.add(item);
   }

   /**
    * Inserts the supplied item at the specified position within the list of
    * related items.
    *
    * @param item A reference to an existing item. At this time, no
    *    validation is performed that this is an existing item or that it is
    *    an allowed type. This will be done when the item is saved.
    *
    * @param pos The order within the existing set to add this item. If pos
    *    is less than 0, 0 is used and if it is greater than the length of the
    *    list, the item is appended.
    */
   public void addRelatedItem( PSLocator item, int pos )
   {
      if ( null == item )
         throw new IllegalArgumentException("item locator must be supplied");

      if ( pos < 0 )
         pos = 0;
      if ( pos >= m_relatedItems.size())
         m_relatedItems.add(item.clone());
      else
         m_relatedItems.add(pos, item.clone());
   }

   /**
    * Creates an instance from a previously serialized (using <code>toXml
    * </code>) one.
    *
    * @param source A valid element that meets the dtd defined in the
    *    description of {@link #toXml(Document)}. Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSSlot(Element source)
      throws PSUnknownNodeTypeException
   {
      fromXml(source, null, null);
   }

   /**
    * This is the name of the root element in the serialized version of this
    * object.
    *
    * @return The root name, never <code>null</code> or empty.
    */
   public static String getNodeName()
   {
      return "PSXSlot";
   }

   /**
    * A numeric value that uniquely identifies this slot.
    *
    * @return The id that was passed into the ctor or found in the serialized
    *    instance, if created from an Element.
    */
   public int getSlotId()
   {
      return m_slotId;
   }

   /**
    * The name is a unique, case-insensitive identifier for a slot.
    *
    * @return The name supplied in the ctor.
    */
   public String getSlotName()
   {
      return m_name;
   }

   /**
    * A short message that describes what this slot is for.
    *
    * @return The description passed in the ctor, or empty if <code>null</code>
    *    was passed. Never <code>null</code>.
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Overrides the base class to compare each of the member properties. All
    * members except the name and allowed variants are compared for exact
    * matches. The name is compared case insensitive and the allowed variants
    * are compared order insensitive.
    *
    * @return <code>true</code> if all members are equal as defined above,
    *    otherwise <code>false</code> is returned.
    */
   public boolean equals( Object o )
   {
      if ( !(o instanceof PSSlot ))
         return false;

      PSSlot slot = (PSSlot) o;

      if (m_slotId != slot.m_slotId)
         return false;
      if ( !m_name.equalsIgnoreCase(slot.m_name))
         return false;
      if (!m_description.equals(slot.m_description))
         return false;
      if (!m_relatedItems.equals(slot.m_relatedItems))
         return false;
      if (!m_allowedVariants.equals(slot.m_allowedVariants))
         return false;
      return true;
   }


   /**
    * Must be overridden to fulfill contract of this method as described in
    * Object.
    *
    * @return A value computed by concatenating all of the properties except
    *    the related content and allowed variants into one string, taking the
    *    hashCode of that and adding the hashcode of the list and map. The
    *    name is lowercased before it is concatenated.
    */
   public int hashCode()
   {
      return (m_name.toLowerCase() + m_description + m_slotId).hashCode()
            + m_relatedItems.hashCode() + m_allowedVariants.hashCode();
   }

   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following dtd:
    * <pre>
    * <ELEMENT PSXItemSlot (Description, AllowedVariants?)>
    * <ATTLIST PSXItemSlot
    *    id       CDATA #REQUIRED
    *    name     CDATA #REQUIRED
    *    >
    * <ELEMENT Description (#PCDATA)>
    * <ELEMENT AllowedVariants (Variant+)>
    * <ELEMENT Variant EMPTY>
    * <ATTLIST Variant
    *    contentTypeId  CDATA    #REQUIRED
    *    variantId      CDATA    #REQUIRED
    *    >
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
      //todo:
      return null;
   }

   /**
    * Replaces the data in this instance with that in a previously serialized
    * version. If any data is invalid, an exception is thrown.
    *
    * @param sourceNode An xml fragment conforming to the dtd described in
    *    {@link #toXml(Document) toXml}. Never <code>null</code>.
    *
    * @param parentDoc Unused.
    *
    * @param parentComponents Unused.
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode must be supplied");
      //todo:
   }

   /**
    * See {@link #PSSlot(String name, int id, String description)
    * ctor} for description. Set in ctor or <code>toXml</code> method.
    * Never <code>null</code> or empty after construction.
    */
   private String m_name;


   /**
    * See {@link #PSSlot(String,int,String,Map) ctor} for description.
    */
   private int m_slotId;

   /**
    * See {@link #PSSlot(String,int,String,Map) ctor} for description.
    * Never <code>null</code> (may be empty).
    */
   private String m_description = "";

   /**
    * See ctor for description. Never <code>null</code>, may be empty.
    */
   private Map m_allowedVariants = new HashMap();

   /**
    * Contains the actual related items. Each member of the list is a PSLocator.
    * Never <code>null</code>, may be empty.
    */
   private List m_relatedItems = new ArrayList();
}
