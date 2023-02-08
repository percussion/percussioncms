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
