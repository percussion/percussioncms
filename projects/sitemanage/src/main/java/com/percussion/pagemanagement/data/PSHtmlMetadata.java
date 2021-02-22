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

package com.percussion.pagemanagement.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.percussion.share.data.PSAbstractDataObject;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

/**
 * Metadata fields to be saved to template
 * 
 * @author Luis
 * @author adamgent
 */
@XmlRootElement(name="HtmlMetadata")
public class PSHtmlMetadata  extends PSAbstractDataObject implements IPSHtmlMetadata {
    

    private static final long serialVersionUID = 1L;
    @NotNull
    @NotEmpty
    private String id;
    private String additionalHeadContent = "";
    private String afterBodyStartContent = "";
    private String beforeBodyCloseContent = "";
    private String protectedRegion = "";
    private String protectedRegionText = "";
    private PSMetadataDocType docType;
    private String description;
    
    
    /**
     * Id of the page or template.
     * @return never <code>null</code> or empty.
     */
    @XmlElement
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    public String getAdditionalHeadContent()
    {
        return additionalHeadContent;
    }

    /**
     * {@inheritDoc}
     */
    public void setAdditionalHeadContent(String additionalHeadContent)
    {
        this.additionalHeadContent = additionalHeadContent;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getAfterBodyStartContent()
    {
        return afterBodyStartContent;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setAfterBodyStartContent(String afterBodyStartContent)
    {
        this.afterBodyStartContent = afterBodyStartContent;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getBeforeBodyCloseContent()
    {
        return beforeBodyCloseContent;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setBeforeBodyCloseContent(String beforeBodyCloseContent)
    {
        this.beforeBodyCloseContent = beforeBodyCloseContent;
    }

    /**
     * {@inheritDoc}
     */
    public String getProtectedRegion()
    {
        return protectedRegion;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setProtectedRegion(String protectedRegion)
    {
        this.protectedRegion = protectedRegion;
    }
    
    /**
     * {@inheritDoc}
     */
    
    public String getProtectedRegionText()
    {
        return protectedRegionText;
    }
    
    /**
     * {@inheritDoc}
     */
    
    public void setProtectedRegionText(String protectedRegionText)
    {
        this.protectedRegionText = protectedRegionText;
    }

    /**
     * {@inheritDoc}
     */
    public PSMetadataDocType getDocType()
    {
        return docType;
    }

    /**
     * {@inheritDoc}
     */
    public void setDocType(PSMetadataDocType docType)
    {
        this.docType = docType;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }
}
