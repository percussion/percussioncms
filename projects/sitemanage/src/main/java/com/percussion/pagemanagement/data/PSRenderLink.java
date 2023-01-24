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
import javax.xml.bind.annotation.XmlTransient;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinitionType;

/**
 * 
 * Represents a rendered link.
 * 
 * @author adamgent
 *
 */
@XmlRootElement(name = "RenderLink")
public class PSRenderLink
{


    private String url;
    private transient PSResourceDefinition resourceDefinition;
    
    private PSResourceDefinitionType resourceType;
    private String resourceDefinitionId;

    

    public PSRenderLink()
    {
    }
    
    public PSRenderLink(String url, PSResourceDefinition resourceDefinition)
    {
        super();
        this.url = url;
        setResourceDefinition(resourceDefinition);
    }

    @XmlTransient
    public PSResourceDefinition getResourceDefinition()
    {
        return resourceDefinition;
    }

    public void setResourceDefinition(PSResourceDefinition resourceDefinition)
    {
        this.resourceDefinition = resourceDefinition;
        if (resourceDefinition != null) {
            setResourceType(resourceDefinition.getResourceType());
            setResourceDefinitionId(resourceDefinition.getUniqueId());
        }
    }

/**
    * Gets value set by setter.
    * see setUrl
    * @return maybe <code>null</code>.
    */
    public String getUrl()
    {
        return url;
    }

    /**
     * Sets url for link item
     * 
     * @param url
     */
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    
    /**
     * Gets the resource definition unique id for this link.
     * This maybe null if the link was created outside
     * of the link service.
     * 
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

    /**
     * Gets value set by setter.
     * see setResourceType
     * @return maybe <code>null</code>.
     */
    public PSResourceDefinitionType getResourceType()
    {
        return resourceType;
    }
    
    

    
    public void setResourceType(PSResourceDefinitionType resourceType)
    {
        this.resourceType = resourceType;
    }

    @Override
    public String toString()
    {
        return url;
    }


   
}
