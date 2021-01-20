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

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import com.percussion.share.data.PSAbstractDataObject;

/**
 * 
 * Represents a request to get an inline resource link.
 * The link service will convert this object to a
 * {@link PSInlineRenderLink}.
 * <p>
 * This object can use legacy template names instead of resource definitions for 
 * the inline link generator.
 * 
 * @author adamgent
 * @see PSInlineRenderLink
 */
@XmlRootElement(name="InlineLinkRequest")
public class PSInlineLinkRequest extends PSAbstractDataObject
{


    private static final long serialVersionUID = 1L;

    @NotNull
    @NotBlank
    private String targetId;
    private String resourceDefinitionId;
    private String thumbResourceDefinitionId;
    
    
    /**
     * The id of the asset resource that we are linking to.
     * @return never <code>null</code>.
     */
    public String getTargetId()
    {
        return targetId;
    }

    public void setTargetId(String contentId)
    {
        this.targetId = contentId;
    }

    public String getThumbResourceDefinitionId()
    {
        return thumbResourceDefinitionId;
    }
    public void setThumbResourceDefinitionId(String thumbResourceDefinitionId)
    {
        this.thumbResourceDefinitionId = thumbResourceDefinitionId;
    }

    /**
     * The fully qualified resource definition id.
     * @return maybe <code>null</code>.
     */
    public String getResourceDefinitionId()
    {
        return resourceDefinitionId;
    }

    public void setResourceDefinitionId(String resourceDefinitionId)
    {
        this.resourceDefinitionId = resourceDefinitionId;
    }

}
