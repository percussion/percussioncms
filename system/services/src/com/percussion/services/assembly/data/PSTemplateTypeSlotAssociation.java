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
package com.percussion.services.assembly.data;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Persist an association between a slot, template and content type.
 * 
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSTemplateTypeSlotAssociation")
@Table(name = "RXSLOTCONTENT")
public class PSTemplateTypeSlotAssociation implements Serializable
{
   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("template-type-slot-association",
            PSTemplateTypeSlotAssociation.class);
   }

   /**
    * Serial id identifies versions of serialized data
    */
   private static final long serialVersionUID = 1L;



   @EmbeddedId
   PSTemplateTypeSlotAssociationPK id;

   @Column(name = "VERSION")
   Integer version = 0;


   /**
    * No args default ctor
    */
   public PSTemplateTypeSlotAssociation() {
   }

   /**
    * Ctor
    * 
    * @param ctype the content type guid, never <code>null</code>
    * @param template the template guid, never <code>null</code>
    * @param slotId the slot id
    */
   public PSTemplateTypeSlotAssociation(IPSGuid ctype, IPSGuid template,
                                        long slotId) {
      if (ctype == null)
      {
         throw new IllegalArgumentException("ctype may not be null");
      }
      if (template == null)
      {
         throw new IllegalArgumentException("template may not be null");
      }
      id = new PSTemplateTypeSlotAssociationPK(template.longValue(), ctype.longValue(), slotId);
   }


   public PSTemplateTypeSlotAssociationPK getId() {
      return id;
   }

   public void setId(PSTemplateTypeSlotAssociationPK id) {
      this.id = id;
   }

   /**
    * @return Returns the contentTypeId.
    */
   public long getContentTypeId()
   {
      if (id != null)
         return id.getContentTypeId();
      else
         return 0;
   }

   /**
    * @param contentTypeId The contentTypeId to set.
    */
   public void setContentTypeId(long contentTypeId)
   {
      if (id == null)
      {
         id = new PSTemplateTypeSlotAssociationPK();
      }
      id.setContentTypeId(contentTypeId);
   }

   /**
    * @return Returns the slotId.
    */
   public long getSlotId()
   {
      if (id != null)
         return id.getSlotId();
      else
         return 0;
   }

   /**
    * @param slotId The slotId to set.
    */
   public void setSlotId(long slotId)
   {

      if (id == null)
      {
         id = new PSTemplateTypeSlotAssociationPK();
      }
      id.setSlotId(slotId);
   }


   /**
    * @return Returns the contentTypeId.
    */
   public long getTemplateId()
   {
      if (id != null)
         return id.getTemplateId();
      else
         return 0;
   }

   /**
    * @param templateId The contentTypeId to set.
    */
   public void setTemplateId(long templateId)
   {
      if (id == null)
      {
         id = new PSTemplateTypeSlotAssociationPK();
      }
      id.setTemplateId(templateId);
   }
   /**
    * @return Returns the version.
    */
   public Integer getVersion()
   {
      return version;
   }

   /**
    * @param version The version to set.
    */
   public void setVersion(Integer version)
   {
      this.version = version;
   }


   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSTemplateTypeSlotAssociation)) return false;
      PSTemplateTypeSlotAssociation that = (PSTemplateTypeSlotAssociation) o;
      return Objects.equals(id, that.id);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id);
   }
}
