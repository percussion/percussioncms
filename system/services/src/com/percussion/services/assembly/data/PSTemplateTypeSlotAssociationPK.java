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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;


/**
 * Primary key class for association
 *
 * @author dougrand
 */
@Embeddable
public class PSTemplateTypeSlotAssociationPK implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 8771284272215837089L;

    @Column(name="VARIANTID", nullable = false)
    long templateId;

    @Column(name = "CONTENTTYPEID", nullable = false)
    long contentTypeId;

    @Column(name = "SLOTID")
    long slotId;

    PSTemplateTypeSlotAssociationPK(long templateId, long contentTypeId, long slotId)
    {
        this.templateId = templateId;
        this.contentTypeId = contentTypeId;
        this.slotId = slotId;
    }

    public PSTemplateTypeSlotAssociationPK() {

    }

    /**
     * @return Returns the contentTypeId.
     */
    public long getContentTypeId()
    {
        return contentTypeId;
    }
    /**
     * @param contentTypeId The contentTypeId to set.
     */
    public void setContentTypeId(long contentTypeId)
    {
        this.contentTypeId = contentTypeId;
    }
    /**
     * @return Returns the slotId.
     */
    public long getSlotId()
    {
        return slotId;
    }
    /**
     * @param slotId The slotId to set.
     */
    public void setSlotId(long slotId)
    {
        this.slotId = slotId;
    }
    /**
     * @return Returns the templateId.
     */
    public long getTemplateId()
    {
        return templateId;
    }
    /**
     * @param templateId The templateId to set.
     */
    public void setTemplateId(long templateId)
    {
        this.templateId = templateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSTemplateTypeSlotAssociationPK)) return false;
        PSTemplateTypeSlotAssociationPK that = (PSTemplateTypeSlotAssociationPK) o;
        return templateId == that.templateId &&
                contentTypeId == that.contentTypeId &&
                slotId == that.slotId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateId, contentTypeId, slotId);
    }

    @Override
    public String toString() {
        return "PSTemplateTypeSlotAssociationPK{" +
                "templateId=" + templateId +
                ", contentTypeId=" + contentTypeId +
                ", slotId=" + slotId +
                '}';
    }
}
