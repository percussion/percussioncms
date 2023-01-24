/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
