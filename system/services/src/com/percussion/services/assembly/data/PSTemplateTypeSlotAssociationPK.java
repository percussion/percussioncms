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
