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
import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This read-only DB component represents one row from the
 * RXSLOTCONTENT table. The physical meaning: each row in this table 
 * represents the allowed content variant into the slot represented
 * by the slotid. 
 * 
 */
public class PSSlotTypeContentTypeVariant extends PSDbComponent
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
   public PSSlotTypeContentTypeVariant(Element source)
      throws PSUnknownNodeTypeException
   {
      super(source);
      fromXml(source);
   }

   /**
    * Constructs an instance of <code>PSSlotTypeContentTypeVariant</code> from a
    * key representation.
    * 
    * @param key
    */
   private PSSlotTypeContentTypeVariant(PSKey key)
   {
      super(key);
   }
   
   /**
    * @return Slot Id.
    */
   public int getSlotId()
   {
      return getKeyPartInt(KEY_SLOTID, -1);
   }


   /**
    * @return ContentTypeId value.
    */
   public long getContentTypeId()
   {
      return getKeyPartLong(KEY_CONTENTTYPEID, -1);
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
    * <!ELEMENT PSXKey (SLOTID, CONTENTTYPEID, VARIANTID )>
    * <!ELEMENT VARIANTID (#PCDATA)>
    * <!ELEMENT CONTENTTYPEID (#PCDATA)>
    * <!ELEMENT SLOTID (#PCDATA)>
    * <!ATTLIST PSXKey needGenerateId (yes | no ) "no">
    * <!ATTLIST PSXKey isPersisted (yes | no ) "yes">
    * <!ELEMENT PSXSlotTypeContentTypeVariant (PSXKey )>
    * <!ATTLIST  PSXSlotTypeContentTypeVariant state CDATA #REQUIRED>
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
      throw new UnsupportedOperationException("PSSlotTypeContentTypeVariant is read-only.");
   }

   public Object clone()
   {
      PSSlotTypeContentTypeVariant copy = (PSSlotTypeContentTypeVariant)super.clone();

      return copy;
   }

   /**
    * Creates the correct key for this component.
    */
   public static PSKey createKey(int slotid)
   {
      if (slotid < 0)
         throw new IllegalArgumentException("slotid may not be < 0");

      return createKey(slotid, -1, -1);
   }
   
   /**
     * Creates the correct key for this component.
     * @param slotId the slot id. 
     * @param contenttypeid the content type id.
     * @param variantId the variant id.
     * 
     * @return the created key, never <code>null</code>.
     */
   private static PSKey createKey(long slotId, long contenttypeid, long variantId)
   {
      String[] keyNames = {KEY_SLOTID, KEY_CONTENTTYPEID, KEY_VARIANTID};
      String[] keyValues = {"" + slotId, "" + contenttypeid, "" + variantId};
      return new PSKey(keyNames, keyValues, true);
   }

   /**
    * Creates a new <code>PSSlotTypeContentTypeVariant</code> instance using
    * the specified ids. Method has package protection to allow access to
    * other objectstore objects.
    * 
    * @param slotId the slot identifier
    * @param contenttypeId the content type identifier
    * @param variantId the variant identifier
    * @return a new <code>PSSlotTypeContentTypeVariant</code> instance, never
    *         <code>null</code>.
    */
   static PSSlotTypeContentTypeVariant create(long slotId, long contenttypeId,
         long variantId)
   {
      if (slotId < 0)
         throw new IllegalArgumentException("slotId may not be < 0");

      PSKey key = createKey(slotId, contenttypeId, variantId);
      return new PSSlotTypeContentTypeVariant(key);
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
      if ( !(o instanceof PSSlotTypeContentTypeVariant ))
         return false;

      PSSlotTypeContentTypeVariant type = (PSSlotTypeContentTypeVariant) o;
      
      if(getContentTypeId()!=type.getContentTypeId())
         return false;
      if(getSlotId()!=type.getSlotId())
         return false;
      if(getVariantId()!=type.getVariantId())
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
         KEY_SLOTID
            + getContentTypeId()
            + KEY_CONTENTTYPEID
            + getSlotId()
            + KEY_VARIANTID
            + getVariantId())
         .hashCode();
   }

   private final static String KEY_SLOTID = "SLOTID";
   private final static String KEY_CONTENTTYPEID = "CONTENTTYPEID";   
   private final static String KEY_VARIANTID = "VARIANTID";
}
