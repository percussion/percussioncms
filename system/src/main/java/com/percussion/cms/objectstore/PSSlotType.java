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
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;

/**
 * The PSSlotType represents one row of the RXSLOTTYPE table.
 * This is a read-only implementation of the PSDbComponent.
 */
public class PSSlotType extends PSDbComponent
{
   /**
    * Creates an instance from a previously serialized (using <code>toXml
    * </code>) object.
    *
    * @param source A valid element that meets the dtd defined in the
    *    description of {@link #toXml(Document)}. Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSSlotType(Element source)
      throws PSUnknownNodeTypeException
   {
      super(source);
      fromXml(source);
   }

   /**
    * Constructs a <code>PSSlotType</code> instance from an assembly service
    * representation of the slot.
    * 
    * @param slot the assembly slot, never <code>null</code>
    */
   public PSSlotType(IPSTemplateSlot slot)
   {
      super(new PSKey(new String[]
      {KEY_SLOTID}, new int[]
      {(int) slot.getGUID().longValue()}, false));

      if (slot.getSlottypeEnum() == IPSTemplateSlot.SlotType.INLINE)
         m_slotType = 1;
      else
         m_slotType = 0;

      m_systemSlot = slot.isSystemSlot() ? 1 : 0;
      m_allowedRelationshipTypeName = slot.getRelationshipName();
      m_slotName = slot.getName();
      m_slotDesc = slot.getDescription();

      m_slotVariants = new PSSlotTypeContentTypeVariantSet();
      Collection<PSPair<IPSGuid, IPSGuid>> associations = slot
            .getSlotAssociations();
      long slotId = slot.getGUID().longValue();
      for (PSPair<IPSGuid, IPSGuid> pair : associations)
      {
         long contentTypeId = pair.getFirst().longValue();
         long templateId = pair.getSecond().longValue();
         PSSlotTypeContentTypeVariant sv = PSSlotTypeContentTypeVariant.create(
               slotId, contentTypeId, templateId);
         m_slotVariants.add(sv);
      }
   }

   /**
    * @return slot id value.
    */
   public int getSlotId()
   {
      return getKeyPartInt(KEY_SLOTID, -1);
   }
   
   /**
    * @return slot name, may be <code>null</code>.
    */
   public String getSlotName()
   {
      return m_slotName;
   }
   
   /**
    * @return slot description, may be <code>null</code>.
    */
   public String getSlotDesc()
   {
      return m_slotDesc;
   }

   /**
    * @return system slot flag as 1 or 0.
    */
   public int getSystemSlot()
   {
      return m_systemSlot;
   }
   
   /**
    * @return slot type id.
    */
   public int getSlotType()
   {
      return m_slotType;
   }
   
   public String getAllowedRelationshipType()
   {
      return m_allowedRelationshipTypeName;
   }
   
   /**
    * Returns slot variants Set.
    * @return slots variants as PSVariantSlotTypeSet,
    * never <code>null</code>, may be <code>empty</code>.
    */
   public PSSlotTypeContentTypeVariantSet getSlotVariants()
   {
      return m_slotVariants;
   }

   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following dtd:
    * <pre>
    * <!ELEMENT PSXSlotType (PSXKey, SlotName?, SlotDesc?, PSXSlotTypeContentTypeVariantSet)>
    * <!ELEMENT PSXSlotTypeContentTypeVariantSet (#PCDATA)>
    * <!ELEMENT SLOTID (#PCDATA)>
    * <!ELEMENT PSXKey (SLOTID )>
    * <!ELEMENT SlotName (#PCDATA)>
    * <!ELEMENT SlotDesc (#PCDATA)>
    * <!ATTLIST PSXKey needGenerateId (yes | no ) "no">
    * <!ATTLIST PSXKey isPersisted (yes | no ) "yes">
    * <!ATTLIST  PSXSlotType slotType CDATA #REQUIRED>
    * <!ATTLIST  PSXSlotType systemSlot CDATA #REQUIRED>
    * <!ATTLIST  PSXSlotType slotId CDATA #IMPLIED>
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

      Element root = super.toXml(doc);
      
      root.setAttribute(XML_ATTR_systemSlot, "" + m_systemSlot);
      root.setAttribute(XML_ATTR_slotType, "" + m_slotType);
      root.setAttribute(
         XML_ATTR_allowedRelationshipName,
         "" + m_allowedRelationshipTypeName);
      
      PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_SlotName, m_slotName);
         
      PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_SlotDesc, m_slotDesc);
      
      if (m_slotVariants != null)
         m_slotVariants.toXml(doc);
      
      return root;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSCmsComponent#fromXml(org.w3c.dom.Element)
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode must be supplied");

      super.fromXml(sourceNode);

      m_systemSlot = PSXMLDomUtil.checkAttributeInt(sourceNode,
         XML_ATTR_systemSlot, false);
         
      m_slotType = PSXMLDomUtil.checkAttributeInt(sourceNode,
               XML_ATTR_slotType, false);
            
      m_allowedRelationshipTypeName =
         PSXMLDomUtil.checkAttribute(
            sourceNode,
            XML_ATTR_allowedRelationshipName,
            false);
      if (m_allowedRelationshipTypeName.trim().length() < 1)
         m_allowedRelationshipTypeName =
            PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY;

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      
      Element el = walker.getNextElement(
         XML_ELEM_SlotName, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_slotName = PSXmlTreeWalker.getElementData(el);
         
      walker.setCurrent(sourceNode);
      
      el = walker.getNextElement(
         XML_ELEM_SlotDesc,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_slotDesc = PSXmlTreeWalker.getElementData(el);
         
      walker.setCurrent(sourceNode);
      
      // get slot variants collection element from which we create
      // PSSlotTypeContentTypeVariantSet
      el =
         walker.getNextElement(
            PSSlotTypeContentTypeVariantSet.XML_NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      
      if (m_slotVariants == null)
         m_slotVariants = new PSSlotTypeContentTypeVariantSet();
      if (el != null) 
         m_slotVariants.fromXml(el);
   }

   /**
    * See {@link IPSDbComponent#toDbXml(Document, Element, IPSKeyGenerator,
    *     PSKey)}.
    * Since this is a read-only object, this is a not supported operation.
    * @throws UnsupportedOperationException always.
    */
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
      PSKey parent) throws PSCmsException
   {
      throw new UnsupportedOperationException("PSSlotType is read-only.");
   }

   public Object clone()
   {
      PSSlotType copy = (PSSlotType)super.clone();

      copy.m_slotName = m_slotName;
      copy.m_slotDesc = m_slotDesc;
      copy.m_systemSlot = m_systemSlot;
      copy.m_slotType = m_slotType;
      copy.m_slotVariants = m_slotVariants; 
   
      return copy;
   }

   /**
    * Creates the correct key for this component.
    * The PSDbComponent createKey is not used because we need a PSLocator
    * rather than a generic PSKey.
    */
   public static PSKey createKey(int slotId)
   {
      if (slotId < 0)
         throw new IllegalArgumentException("variantid may not be < 0");

      return new PSSimpleKey(KEY_SLOTID, "" + slotId);    
   }
   
   /**
    * Creates the correct key for this component.
    */
   public static PSKey[] createKeys(int[] slotIds)
   {
      if (slotIds == null || slotIds.length < 1)
         throw new IllegalArgumentException("slotIds may not be null or empty");
   
      PSKey keys[] = new PSKey[slotIds.length];
         
      for (int i = 0; i < slotIds.length; i++)
      {
         if (slotIds[i]< 1)
            throw new IllegalArgumentException("slotIds may not be < 0");
      
         keys[i] = new PSSimpleKey(KEY_SLOTID, "" + slotIds[i]); 
      }   
   
      return keys;  
   }

   /**
    * Override to create our own Key which is 
    * {@link com.percussion.design.objectstore.PSLocator}.
    */
   protected PSKey createKey(Element el) throws PSUnknownNodeTypeException
   {
      if (el == null)
         throw new IllegalArgumentException("Source element cannot be null.");

      return new PSKey(el);
   }
   
   /**
    * Overrides the base class to compare each of the member properties. All
    * string members'omparison is case insensitive.
    *
    * @param o The comparee. If null or not an instance of this class,
    *    <code>false</code> is returned.
    *
    * @return <code>true</code> if all members are equal as defined above,
    *    otherwise <code>false</code> is returned.
    */
   public boolean equals( Object o )
   {
      if ( !(o instanceof PSSlotType ))
         return false;

      PSSlotType type = (PSSlotType) o;

      if (!m_allowedRelationshipTypeName
         .equalsIgnoreCase(type.m_allowedRelationshipTypeName))
         return false;
      if (!m_slotDesc.equalsIgnoreCase(type.m_slotDesc))
         return false;
      if (!m_slotName.equalsIgnoreCase(type.m_slotName))
         return false;
      if (m_slotType != type.m_slotType)
         return false;
      if (m_systemSlot != type.m_systemSlot)
         return false;
      if (!m_slotVariants.equals(type.m_slotVariants))
         return false;

      return true;
   }

   /**
    * Must be overridden because we overrode equals.
    *
    * @return A value computed by concatenating all of the properties into one
    *    string and taking the hashCode of that. The name is lowercased before
    *    it is concatenated.
    */
   public int hashCode()
   {
      return (
         m_allowedRelationshipTypeName.toLowerCase().hashCode()
            + m_slotDesc.toLowerCase().hashCode()
            + m_slotName.toLowerCase().hashCode()
            + ("SLOTTYPE" + m_slotType).hashCode()
            + ("SYSTEMSLOT"+m_systemSlot).hashCode()
            + m_slotVariants.hashCode());
   }

   /**
    * ContentVariants Table Key Name.
    */
   private final static String KEY_SLOTID = "SLOTID";
   
   /*
    * All the attributes for variant. 
    */
   private String m_slotName = "";
   private String m_slotDesc = "";
   private int m_systemSlot = -1;
   private int m_slotType = -1;
   private String m_allowedRelationshipTypeName =
      PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY;

   
   /**
    * Slot Variants collection. Initialized by fromXml,
    * may be <code>null</code>.
    */
   private PSSlotTypeContentTypeVariantSet m_slotVariants = null;
      
   // Private constants for XML attribute and element name
   private final static String XML_ATTR_systemSlot = "systemSlot";
   private final static String XML_ATTR_slotType = "slotType";
   private final static String XML_ATTR_allowedRelationshipName =
      "allowedRelationship";
   
   private final static String XML_ELEM_SlotName = "SlotName";
   private final static String XML_ELEM_SlotDesc = "SlotDesc";
}   
