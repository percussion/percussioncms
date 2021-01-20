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

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.utils.guid.IPSGuid;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This read-only DB component represents one row from the
 * RXVARIANTSLOTTYPE table.
 * @deprecated use the assembly service instead
 */
public class PSVariantSlotType extends PSDbComponent
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
   public PSVariantSlotType(Element source)
      throws PSUnknownNodeTypeException
   {
      super(source);
      fromXml(source);
   }

   /**
    * Create a record from information from the assembly manager. Note that due
    * to Java, we cannot check the arguments before calling super.
    * 
    * @param guid the guid of the variant, never <code>null</code>
    * @param slot the slot, never <code>null</code>
    */
   public PSVariantSlotType(IPSGuid guid, IPSTemplateSlot slot) {
      super(createKey((int) guid.longValue(), (int) slot.getGUID().longValue()));
   }

   /**
    * @return Slot Id.
    */
   public int getSlotId()
   {
      return getKeyPartInt(KEY_SLOTID, -1);
   }


   /**
    * @return variant id.
    */
   public int getVariantId()
   {
      return getKeyPartInt(KEY_VARIANTID, -1); 
   }
   
   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following dtd:
    * <pre>
    * <!ELEMENT VARIANTID (#PCDATA)>
    * <!ELEMENT SLOTID (#PCDATA)>
    * <!ELEMENT PSXKey (VARIANTID, SLOTID)>
    * <!ATTLIST PSXKey isPersisted (yes | no ) "yes">
    * <!ATTLIST PSXKey needGenerateId (yes | no ) "no">
    * <!ELEMENT PSXVariantSlotType (PSXKey )>
    * <!ATTLIST  PSXVariantSlotType state CDATA #REQUIRED>
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
      throw new UnsupportedOperationException("PSVariantSlotType is read-only.");
   }

   public Object clone()
   {
      PSVariantSlotType copy = (PSVariantSlotType)super.clone();

      return copy;
   }

   /**
    * Creates the correct key for this component.
    * 
    * @param variantid the variant id
    * 
    * @return the created key, never <code>null</code>. 
    */
   public static PSKey createKey(int variantid)
   {
      if (variantid < 0)
         throw new IllegalArgumentException("variantid may not be < 0");

      return createKey(variantid, -1);
   }
   
   /**
    * Creates the correct key for this component.
    * @param variantId the variant id
    * @param slotId the slot id
    * 
    * @return the created key, never <code>null</code>.
    */
   private static PSKey createKey(int variantId, int slotId)
   {
      String[] keyNames = {KEY_VARIANTID, KEY_SLOTID};
      String[] keyValues = {"" + variantId, "" + slotId};
      return new PSKey(keyNames, keyValues, true);
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
    * Overrides the base class to compare each of the member properties. This 
    * class is an exception for this methid's implementation in that we compare 
    * the key values to decide if two objects are equal or not. This is because
    * of the fact that this class has no other memeber data other than the key
    * values. 
    *
    * @param o The comparee. If null or not an instance of this class,
    *    <code>false</code> is returned.
    *
    * @return <code>true</code> if all members are equal as defined above,
    *    otherwise <code>false</code> is returned.
    */
   public boolean equals( Object o )
   {
      if (!(o instanceof PSVariantSlotType))
         return false;

      PSVariantSlotType type = (PSVariantSlotType) o;

      if (getSlotId() != type.getSlotId())
         return false;
      if (getVariantId() != type.getVariantId())
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
         (KEY_SLOTID + getSlotId()).hashCode()
            + (KEY_VARIANTID + getVariantId()).hashCode());
   }

   private final static String KEY_VARIANTID = "VARIANTID";
   private final static String KEY_SLOTID = "SLOTID";
}
